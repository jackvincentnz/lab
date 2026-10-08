package lab.mops.ai.infrastructure.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import lab.mops.ai.application.chat.ChatCommandService;
import lab.mops.ai.application.chat.ChatContextBuilder;
import lab.mops.ai.application.chat.ChatEventHandler;
import lab.mops.ai.application.chat.MessageMapper;
import lab.mops.ai.application.chat.StartChatCommand;
import lab.mops.ai.application.chat.Tool;
import lab.mops.ai.application.chat.ToolDefinition;
import lab.mops.ai.application.chat.ToolProvider;
import lab.mops.ai.application.chat.completions.AssistantMessage;
import lab.mops.ai.application.chat.completions.CompletionService;
import lab.mops.ai.domain.chat.Chat;
import lab.mops.ai.domain.chat.ChatRepository;
import lab.mops.ai.domain.chat.MessageStatus;
import lab.mops.ai.domain.chat.ToolCall;
import lab.mops.ai.domain.chat.ToolCallId;
import lab.mops.ai.domain.chat.ToolCallStatus;
import lab.mops.config.DatabaseConfig;
import lab.test.TestBase;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

@SpringJUnitConfig(ChatOutboxTest.Config.class)
class ChatOutboxTest extends TestBase {
  @Autowired ChatCommandService commands;
  @Autowired ChatRepository chats;
  @Autowired ChatOutbox outbox;
  @Autowired ChatOutboxWorker worker;
  @Autowired CompletionService completions;
  @Autowired ToolProvider tools;
  @Autowired JdbcTemplate jdbc;
  @Autowired PlatformTransactionManager transactionManager;

  @BeforeEach
  void clean() {
    jdbc.update("DELETE FROM chat_outbox");
    jdbc.update("DELETE FROM outbox_test_effect");
    chats.deleteAll();
    reset(completions, tools);
    when(tools.getTools()).thenReturn(List.of());
  }

  @Test
  void startChat_rolledBack_rollsBackMessageAndWork() {
    var tx = new TransactionTemplate(transactionManager);
    tx.executeWithoutResult(
        status -> {
          commands.startChat(new StartChatCommand(randomString()));
          assertThat(countWork()).isEqualTo(1);
          status.setRollbackOnly();
        });
    assertThat(countWork()).isZero();
    assertThat(chats.count()).isZero();
  }

  @Test
  void startChat_enqueueFails_rollsBackChat() {
    jdbc.execute("ALTER TABLE chat_outbox ADD CONSTRAINT outbox_test_reject CHECK (FALSE)");
    try {
      assertThatThrownBy(() -> commands.startChat(new StartChatCommand(randomString())))
          .isInstanceOf(RuntimeException.class);
      assertThat(chats.count()).isZero();
      assertThat(countWork()).isZero();
    } finally {
      jdbc.execute("ALTER TABLE chat_outbox DROP CONSTRAINT outbox_test_reject");
    }
  }

  @Test
  void save_editedChatRollsBack_rollsBackFollowUpWork() {
    var chat = commands.startChat(new StartChatCommand(randomString()));
    var id = chat.getId();
    var messageCount = chats.getById(id).getMessages().size();
    assertThat(countWork()).isEqualTo(1);
    new TransactionTemplate(transactionManager)
        .executeWithoutResult(
            status -> {
              var loaded = chats.getById(id);
              loaded.addUserMessage(randomString());
              chats.save(loaded);
              status.setRollbackOnly();
            });
    assertThat(chats.getById(id).getMessages()).hasSize(messageCount);
    assertThat(countWork()).isEqualTo(1);
  }

  @Test
  void processNext_newWorkerAfterCommit_deliversAndAcknowledgesAtomically() {
    var chat = commands.startChat(new StartChatCommand(randomString()));
    var content = randomString();
    when(completions.getResponse(anyList())).thenReturn(AssistantMessage.of(content));
    var restarted =
        new ChatOutboxWorker(
            outbox,
            new ChatEventHandler(
                chats, completions, tools, new ChatContextBuilder(new MessageMapper())),
            chats,
            transactionManager);

    assertThat(restarted.processNext()).isTrue();
    assertThat(restarted.processNext()).isFalse();
    assertThat(chats.getById(chat.getId()).getMessages().get(1).getContent()).contains(content);
    assertThat(countWork()).isZero();
  }

  @Test
  void processNext_transactionRollsBack_retainsWorkForRedelivery() {
    var chat = commands.startChat(new StartChatCommand(randomString()));
    doReturn(AssistantMessage.of(randomString())).when(completions).getResponse(anyList());
    new TransactionTemplate(transactionManager)
        .executeWithoutResult(
            status -> {
              worker.processNext();
              assertThat(countWork()).isZero();
              status.setRollbackOnly();
            });
    assertThat(countWork()).isEqualTo(1);
    assertThat(chats.getById(chat.getId()).getMessages().get(1).isPending()).isTrue();
    worker.processNext();
    assertThat(countWork()).isZero();
  }

  @Test
  void processNext_transientFailure_retriesDurablyThenCompletes() {
    var chat = commands.startChat(new StartChatCommand(randomString()));
    when(completions.getResponse(anyList())).thenThrow(new IllegalStateException());
    assertThat(worker.processNext()).isTrue();
    assertThat(countWork()).isEqualTo(1);
    assertThat(jdbc.queryForObject("SELECT attempts FROM chat_outbox", Integer.class)).isEqualTo(1);
    assertThat(chats.getById(chat.getId()).getMessages().get(1).isPending()).isTrue();
    assertThat(worker.processNext()).isFalse();

    makeDue();
    doReturn(AssistantMessage.of(randomString())).when(completions).getResponse(anyList());
    worker.processNext();
    assertThat(chats.getById(chat.getId()).getMessages().get(1).isCompleted()).isTrue();
    assertThat(countWork()).isZero();
  }

  @Test
  void processNext_retriesExhausted_persistsFailure() {
    var chat = commands.startChat(new StartChatCommand(randomString()));
    when(completions.getResponse(anyList())).thenThrow(new IllegalStateException());
    for (var attempt = 0; attempt < ChatOutboxWorker.MAX_ATTEMPTS; attempt++) {
      makeDue();
      worker.processNext();
    }
    var failed = chats.getById(chat.getId()).getMessages().get(1);
    assertThat(failed.getStatus()).isEqualTo(MessageStatus.FAILED);
    assertThat(failed.getContent().orElseThrow()).contains("several attempts");
    assertThat(countWork()).isZero();
  }

  @Test
  void processNext_concurrentWorkers_skipLockedChat() throws Exception {
    var chat = commands.startChat(new StartChatCommand(randomString()));
    var entered = new CountDownLatch(1);
    var release = new CountDownLatch(1);
    when(completions.getResponse(anyList()))
        .thenAnswer(
            invocation -> {
              entered.countDown();
              assertThat(release.await(10, TimeUnit.SECONDS)).isTrue();
              return AssistantMessage.of(randomString());
            });
    var executor = Executors.newSingleThreadExecutor();
    try {
      var running = executor.submit(worker::processNext);
      assertThat(entered.await(10, TimeUnit.SECONDS)).isTrue();
      assertThat(worker.processNext()).isFalse();
      var other = commands.startChat(new StartChatCommand(randomString()));
      doReturn(AssistantMessage.of(randomString())).when(completions).getResponse(anyList());
      assertThat(worker.processNext()).isTrue();
      assertThat(chats.getById(other.getId()).getMessages().get(1).isCompleted()).isTrue();
      release.countDown();
      assertThat(running.get(10, TimeUnit.SECONDS)).isTrue();
      assertThat(chats.getById(chat.getId()).getMessages().get(1).isCompleted()).isTrue();
      assertThat(countWork()).isZero();
    } finally {
      release.countDown();
      executor.shutdownNow();
    }
  }

  @Test
  void processNext_toolFailure_rollsBackEffectsAndRetriesWithoutDuplicateCommit() {
    var chat = completedToolChat();
    var toolCall = chat.getMessages().get(1).getToolCalls().get(0);
    var tool = mock(Tool.class);
    var definition = mock(ToolDefinition.class);
    when(tool.getToolDefinition()).thenReturn(definition);
    when(definition.name()).thenReturn(toolCall.name());
    when(tools.getTools()).thenReturn(List.of(tool));
    when(tool.call(toolCall.arguments()))
        .thenAnswer(
            invocation -> {
              jdbc.update("INSERT INTO outbox_test_effect (id) VALUES (1)");
              throw new IllegalStateException();
            });
    worker.processNext();
    assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM outbox_test_effect", Integer.class))
        .isZero();
    assertThat(
            chats
                .getById(chat.getId())
                .getToolCallById(chat.getMessages().get(1).getId(), toolCall.id())
                .result())
        .isNull();

    makeDue();
    doAnswer(
            invocation -> {
              jdbc.update("INSERT INTO outbox_test_effect (id) VALUES (1)");
              return randomString();
            })
        .when(tool)
        .call(toolCall.arguments());
    worker.processNext();
    assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM outbox_test_effect", Integer.class))
        .isEqualTo(1);
    assertThat(countWork())
        .isEqualTo(1); // The next assistant completion is committed with the result.
  }

  @Test
  void processNext_repositoryMarksRollbackOnly_retainsRetryMetadataAndRollsBackTool() {
    var chat = completedToolChat();
    var messageId = chat.getMessages().get(1).getId();
    var call =
        chat.getToolCallById(messageId, chat.getMessages().get(1).getToolCalls().get(0).id());
    var tool = mock(Tool.class);
    var definition = mock(ToolDefinition.class);
    when(tool.getToolDefinition()).thenReturn(definition);
    when(definition.name()).thenReturn(call.name());
    when(tools.getTools()).thenReturn(List.of(tool));
    when(tool.call(call.arguments()))
        .thenAnswer(
            invocation -> {
              jdbc.update("INSERT INTO outbox_test_effect (id) VALUES (1)");
              return randomString();
            });
    jdbc.execute(
        "ALTER TABLE chat_outbox ADD CONSTRAINT outbox_test_only_original CHECK (message_id = '"
            + messageId
            + "')");
    try {
      worker.processNext();
      assertThat(jdbc.queryForObject("SELECT attempts FROM chat_outbox", Integer.class))
          .isEqualTo(1);
      assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM outbox_test_effect", Integer.class))
          .isZero();
      assertThat(chats.getById(chat.getId()).getToolCallById(messageId, call.id()).result())
          .isNull();
    } finally {
      jdbc.execute("ALTER TABLE chat_outbox DROP CONSTRAINT outbox_test_only_original");
    }
    makeDue();
    worker.processNext();
    assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM outbox_test_effect", Integer.class))
        .isEqualTo(1);
    assertThat(countWork()).isEqualTo(1);
  }

  @Test
  void migrate_existingPendingMessagesAndApprovedTools_recoversWork() {
    var source =
        new DriverManagerDataSource("jdbc:h2:mem:" + randomId() + ";DB_CLOSE_DELAY=-1", "sa", "");
    Flyway.configure().dataSource(source).target("0.1").load().migrate();
    var legacy = new JdbcTemplate(source);
    var chatId = java.util.UUID.randomUUID();
    var pendingId = java.util.UUID.randomUUID();
    var toolMessageId = java.util.UUID.randomUUID();
    legacy.update("INSERT INTO chat (id, version) VALUES (?, 1)", chatId);
    legacy.update(
        "INSERT INTO chat_message (id, chat, chat_key, type, status) VALUES (?, ?, 0, 'ASSISTANT',"
            + " 'PENDING')",
        pendingId,
        chatId);
    legacy.update(
        "INSERT INTO chat_message (id, chat, chat_key, type, status) VALUES (?, ?, 1, 'ASSISTANT',"
            + " 'COMPLETED')",
        toolMessageId,
        chatId);
    legacy.update(
        "INSERT INTO tool_call (id, chat_message, name, status, arguments) VALUES (?, ?, ?,"
            + " 'APPROVED', '{}')",
        randomString(),
        toolMessageId,
        randomString());
    legacy.update(
        "INSERT INTO tool_call (id, chat_message, name, status, arguments) VALUES (?, ?, ?,"
            + " 'PENDING_APPROVAL', '{}')",
        randomString(),
        toolMessageId,
        randomString());

    Flyway.configure().dataSource(source).load().migrate();

    assertThat(legacy.queryForObject("SELECT COUNT(*) FROM chat_outbox", Integer.class))
        .isEqualTo(2);
  }

  @Test
  void enqueue_duplicateEvent_hasOneWorkItem() {
    var chat = commands.startChat(new StartChatCommand(randomString()));
    var work = new ChatOutbox.Work(chat.getId(), chat.getMessages().get(1).getId(), "", 0);
    new TransactionTemplate(transactionManager)
        .executeWithoutResult(
            status -> {
              outbox.onPendingAssistantMessageAdded(work);
              outbox.onPendingAssistantMessageAdded(work);
            });
    assertThat(countWork()).isEqualTo(1);
  }

  @Test
  void save_repositoryTransaction_enqueuesAtomically() {
    chats.save(Chat.start(randomString()));
    assertThat(countWork()).isEqualTo(1);
  }

  private Chat completedToolChat() {
    return new TransactionTemplate(transactionManager)
        .execute(
            status -> {
              var chat = commands.startChat(new StartChatCommand(randomString()));
              jdbc.update("DELETE FROM chat_outbox");
              chat.addPendingToolCalls(
                  chat.getMessages().get(1).getId(),
                  List.of(
                      ToolCall.of(
                          ToolCallId.create(),
                          randomString(),
                          randomString(),
                          ToolCallStatus.APPROVED)));
              return chats.save(chat);
            });
  }

  private int countWork() {
    return jdbc.queryForObject("SELECT COUNT(*) FROM chat_outbox", Integer.class);
  }

  private void makeDue() {
    jdbc.update("UPDATE chat_outbox SET available_at = CURRENT_TIMESTAMP");
  }

  @Configuration
  @EnableTransactionManagement
  @EnableJdbcRepositories(basePackageClasses = ChatRepository.class)
  @Import({
    DatabaseConfig.class,
    ChatCommandService.class,
    ChatOutbox.class,
    ChatOutboxCallback.class,
    ChatOutboxWorker.class,
    ChatEventHandler.class,
    ChatContextBuilder.class,
    MessageMapper.class
  })
  static class Config {
    @Bean
    DataSource dataSource() {
      var source = new DriverManagerDataSource("jdbc:h2:mem:outbox;DB_CLOSE_DELAY=-1", "sa", "");
      Flyway.configure().dataSource(source).load().migrate();
      new JdbcTemplate(source).execute("CREATE TABLE outbox_test_effect (id INTEGER PRIMARY KEY)");
      return source;
    }

    @Bean
    NamedParameterJdbcTemplate namedParameterJdbcTemplate(DataSource source) {
      return new NamedParameterJdbcTemplate(source);
    }

    @Bean
    JdbcTemplate jdbcTemplate(DataSource source) {
      return new JdbcTemplate(source);
    }

    @Bean
    PlatformTransactionManager transactionManager(DataSource source) {
      return new JdbcTransactionManager(source);
    }

    @Bean
    CompletionService completionService() {
      return mock(CompletionService.class);
    }

    @Bean
    ToolProvider toolProvider() {
      return mock(ToolProvider.class);
    }
  }
}
