package lab.mops.ai.infrastructure.outbox;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import lab.mops.ai.domain.chat.ChatId;
import lab.mops.ai.domain.chat.MessageId;
import lab.mops.ai.domain.chat.PendingAssistantMessageAddedEvent;
import lab.mops.ai.domain.chat.ToolCallApprovedEvent;
import lab.mops.ai.domain.chat.ToolCallId;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(propagation = Propagation.MANDATORY)
public class ChatOutbox {
  private final JdbcTemplate jdbc;

  public ChatOutbox(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public void onPendingAssistantMessageAdded(PendingAssistantMessageAddedEvent event) {
    enqueue(event.chatId(), event.pendingAssistantMessageId(), "");
  }

  public void onToolCallApproved(ToolCallApprovedEvent event) {
    enqueue(event.chatId(), event.messageId(), event.toolCallId().toString());
  }

  private void enqueue(ChatId chatId, MessageId messageId, String toolCallId) {
    jdbc.update(
        """
        MERGE INTO chat_outbox o
        USING (VALUES (CAST(? AS UUID), CAST(? AS UUID), CAST(? AS VARCHAR(256))))
          s(chat_id, message_id, tool_call_id)
        ON o.chat_id = s.chat_id AND o.message_id = s.message_id AND o.tool_call_id = s.tool_call_id
        WHEN NOT MATCHED THEN INSERT (chat_id, message_id, tool_call_id)
          VALUES (s.chat_id, s.message_id, s.tool_call_id)
        """,
        chatId.toUUID(),
        messageId.toUUID(),
        toolCallId);
  }

  public Optional<Work> claim() {
    // Lock the chat as well as the work: sibling tools and user commands must serialize.
    return jdbc
        .query(
            """
            SELECT o.* FROM chat_outbox o JOIN chat c ON c.id = o.chat_id
            WHERE o.available_at <= CURRENT_TIMESTAMP
            ORDER BY o.created_at, o.chat_id, o.message_id, o.tool_call_id LIMIT 1 FOR UPDATE SKIP LOCKED
            """,
            (rs, row) -> map(rs))
        .stream()
        .findFirst();
  }

  public Optional<Work> find(Work expected) {
    // Another worker may finish the work between rollback and retry recording.
    return jdbc
        .query(
            """
            SELECT o.* FROM chat_outbox o JOIN chat c ON c.id = o.chat_id
            WHERE o.chat_id = ? AND o.message_id = ? AND o.tool_call_id = ? FOR UPDATE
            """,
            (rs, row) -> map(rs),
            expected.chatId().toUUID(),
            expected.messageId().toUUID(),
            expected.toolCallId())
        .stream()
        .findFirst();
  }

  private Work map(ResultSet rs) throws SQLException {
    return new Work(
        ChatId.fromString(rs.getString("chat_id")),
        MessageId.fromString(rs.getString("message_id")),
        rs.getString("tool_call_id"),
        rs.getInt("attempts"));
  }

  public void complete(Work work) {
    jdbc.update(
        "DELETE FROM chat_outbox WHERE chat_id = ? AND message_id = ? AND tool_call_id = ?",
        work.chatId().toUUID(),
        work.messageId().toUUID(),
        work.toolCallId());
  }

  public void retry(Work work) {
    var delaySeconds = Math.min(60, 1 << work.attempts());
    jdbc.update(
        """
        UPDATE chat_outbox SET attempts = attempts + 1,
          available_at = ? WHERE chat_id = ? AND message_id = ? AND tool_call_id = ?
        """,
        Timestamp.from(Instant.now().plusSeconds(delaySeconds)),
        work.chatId().toUUID(),
        work.messageId().toUUID(),
        work.toolCallId());
  }

  public record Work(ChatId chatId, MessageId messageId, String toolCallId, int attempts)
      implements PendingAssistantMessageAddedEvent {
    @Override
    public MessageId pendingAssistantMessageId() {
      return messageId;
    }

    public boolean isCompletion() {
      return toolCallId.isEmpty();
    }

    public ToolCallApprovedEvent toolEvent() {
      return new ToolCallApprovedEvent(chatId, messageId, ToolCallId.of(toolCallId));
    }
  }
}
