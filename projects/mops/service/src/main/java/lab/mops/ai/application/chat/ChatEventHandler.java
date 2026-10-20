package lab.mops.ai.application.chat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import lab.mops.ai.application.chat.completions.CompletionService;
import lab.mops.ai.application.chat.completions.ToolCall;
import lab.mops.ai.application.chat.completions.ToolResultMessage;
import lab.mops.ai.domain.chat.Chat;
import lab.mops.ai.domain.chat.ChatId;
import lab.mops.ai.domain.chat.ChatRepository;
import lab.mops.ai.domain.chat.MessageId;
import lab.mops.ai.domain.chat.PendingAssistantMessageAddedEvent;
import lab.mops.ai.domain.chat.ToolCallApprovedEvent;
import lab.mops.ai.domain.chat.ToolCallId;
import lab.mops.ai.domain.chat.ToolCallStatus;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ChatEventHandler {

  private static final Logger LOG = LoggerFactory.getLogger(ChatEventHandler.class);

  protected static final int MAX_COMPLETIONS = 10;

  private static final int MAX_SAVE_ATTEMPTS = 3;

  private final ChatRepository chatRepository;

  private final CompletionService completionService;

  private final ToolProvider toolProvider;

  private final ChatContextBuilder chatContextBuilder;

  public ChatEventHandler(
      ChatRepository chatRepository,
      CompletionService completionService,
      ToolProvider toolProvider,
      ChatContextBuilder chatContextBuilder) {
    this.chatRepository = chatRepository;
    this.completionService = completionService;
    this.toolProvider = toolProvider;
    this.chatContextBuilder = chatContextBuilder;
  }

  @Async
  @TransactionalEventListener
  public void onPendingAssistantMessageAdded(PendingAssistantMessageAddedEvent event) {
    var chatId = event.chatId();
    var messageId = event.pendingAssistantMessageId();
    LOG.debug("Handling pending assistant message [{}], in chat [{}]", messageId, chatId);

    try {
      var chat = chatRepository.getById(chatId);
      if (!isPending(chat, messageId)) {
        return;
      }

      var response = respond(chat, messageId);
      update(chatId, latest -> isPending(latest, messageId), response);
    } catch (RuntimeException e) {
      LOG.warn("Failed to respond to message [{}], in chat [{}]", messageId, chatId, e);
      update(
          chatId, latest -> isPending(latest, messageId), latest -> latest.failMessage(messageId));
    }
  }

  private Consumer<Chat> respond(Chat chat, MessageId messageId) {
    var tools = toolProvider.getTools();
    var conversationHistory = new ArrayList<>(chatContextBuilder.buildHistory(chat));

    var completionCounter = 0;
    var response = completionService.getResponse(conversationHistory);
    while (response.hasToolCalls() && !hasPendingToolApprovals(tools, response.getToolCalls())) {
      if (++completionCounter >= MAX_COMPLETIONS) {
        LOG.warn("Message [{}] reached the completion limit", messageId);
        return latest -> latest.failMessage(messageId);
      }

      LOG.debug("Response has tools calls and none need approval");

      conversationHistory.add(response);

      var toolResults = executeToolCalls(response.getToolCalls());
      conversationHistory.addAll(toolResults);

      response = completionService.getResponse(conversationHistory);
    }

    if (hasPendingToolApprovals(tools, response.getToolCalls())) {
      LOG.debug("Response has pending tool calls, adding to chat for approval");
      var toolCalls = mapToolCalls(tools, response.getToolCalls());
      return latest -> latest.addPendingToolCalls(messageId, toolCalls);
    }

    LOG.debug("Response has no pending tool calls, completing pending message");
    var content = response.getContent().orElseThrow();
    return latest -> latest.completeMessage(messageId, content);
  }

  private static boolean isPending(Chat chat, MessageId messageId) {
    return chat.getMessages().stream().anyMatch(m -> m.getId().equals(messageId) && m.isPending());
  }

  private boolean hasPendingToolApprovals(Collection<Tool> tools, Collection<ToolCall> toolCalls) {
    return toolCalls.stream().anyMatch(t -> toolNeedsApproval(tools, t));
  }

  private boolean toolNeedsApproval(Collection<Tool> tools, ToolCall toolCall) {
    return tools.stream()
        .anyMatch(
            t ->
                toolCall.toolName().equals(t.getToolDefinition().name())
                    && t.getToolDefinition().needsApproval());
  }

  private List<ToolResultMessage> executeToolCalls(Collection<ToolCall> toolCalls) {
    var toolResults = new ArrayList<ToolResultMessage>();

    for (var toolCall : toolCalls) {
      var result = executeToolCall(toolCall);

      toolResults.add(new ToolResultMessage(result, toolCall.id(), toolCall.toolName()));
    }

    return toolResults;
  }

  private List<lab.mops.ai.domain.chat.ToolCall> mapToolCalls(
      Collection<Tool> tools, Collection<ToolCall> toolCalls) {
    return toolCalls.stream()
        .map(
            toolCall -> {
              var tool =
                  tools.stream()
                      .filter(t -> t.getToolDefinition().name().equals(toolCall.toolName()))
                      .findFirst()
                      .orElseThrow(
                          () ->
                              new RuntimeException(
                                  "Tool [%s] not available".formatted(toolCall.toolName())));

              var toolCallId =
                  StringUtils.isBlank(toolCall.id())
                      ? ToolCallId.create()
                      : ToolCallId.of(toolCall.id());

              return lab.mops.ai.domain.chat.ToolCall.of(
                  toolCallId,
                  toolCall.toolName(),
                  toolCall.arguments(),
                  tool.getToolDefinition().needsApproval()
                      ? ToolCallStatus.PENDING_APPROVAL
                      : ToolCallStatus.APPROVED);
            })
        .toList();
  }

  @Async
  @TransactionalEventListener
  public void onToolCallApproved(ToolCallApprovedEvent event) {
    var chatId = event.chatId();
    var messageId = event.messageId();
    var toolCallId = event.toolCallId();

    try {
      var toolCall = awaitingResult(chatRepository.getById(chatId), messageId, toolCallId);
      if (toolCall.isEmpty()) {
        return;
      }

      var result =
          executeToolCall(
              new ToolCall(
                  toolCallId.toString(), toolCall.get().name(), toolCall.get().arguments()));

      update(
          chatId,
          latest -> awaitingResult(latest, messageId, toolCallId).isPresent(),
          latest -> latest.recordToolResult(messageId, toolCallId, result));
    } catch (RuntimeException e) {
      LOG.warn("Failed to execute tool call [{}], in chat [{}]", toolCallId, chatId, e);
      update(
          chatId,
          latest -> awaitingResult(latest, messageId, toolCallId).isPresent(),
          latest -> latest.failMessage(messageId));
    }
  }

  private static Optional<lab.mops.ai.domain.chat.ToolCall> awaitingResult(
      Chat chat, MessageId messageId, ToolCallId toolCallId) {
    return chat.getMessages().stream()
        .filter(m -> m.getId().equals(messageId) && m.isCompleted())
        .flatMap(m -> m.getToolCalls().stream())
        .filter(
            t ->
                t.id().equals(toolCallId)
                    && t.status() == ToolCallStatus.APPROVED
                    && t.result() == null)
        .findFirst();
  }

  /**
   * Applies a change to the latest saved chat while the work is still current. Commands and sibling
   * tool calls save the same chat concurrently, so a save that loses the version check reloads the
   * chat instead of repeating the model or tool call.
   */
  private void update(ChatId chatId, Predicate<Chat> isCurrent, Consumer<Chat> change) {
    for (var attempt = 1; ; attempt++) {
      var chat = chatRepository.getById(chatId);
      if (!isCurrent.test(chat)) {
        return;
      }

      change.accept(chat);
      try {
        chatRepository.save(chat);
        return;
      } catch (OptimisticLockingFailureException e) {
        if (attempt == MAX_SAVE_ATTEMPTS) {
          throw e;
        }
      }
    }
  }

  private String executeToolCall(ToolCall toolCall) {
    var tool = getToolByName(toolCall.toolName());

    LOG.debug("Executing tool [{}] with arguments [{}]", toolCall.toolName(), toolCall.arguments());

    var result = tool.call(toolCall.arguments());

    LOG.debug("Tool [{}] returned [{}]", toolCall.toolName(), result);

    return result;
  }

  // TODO: check lookup performance -> move to interface that does exactly what we need
  private Tool getToolByName(String name) {
    return toolProvider.getTools().stream()
        .filter(t -> name.equals(t.getToolDefinition().name()))
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Tool [%s] not available".formatted(name)));
  }
}
