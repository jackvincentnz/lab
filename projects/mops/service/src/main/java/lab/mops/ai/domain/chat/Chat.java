package lab.mops.ai.domain.chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import nz.geek.jack.libs.ddd.domain.Aggregate;
import nz.geek.jack.libs.ddd.domain.NotFoundException;

public class Chat extends Aggregate<ChatId> {

  private final List<Message> messages;

  private Chat(ChatId id, List<Message> messages) {
    super(id);
    this.messages = messages;
  }

  public Message addUserMessage(String content) {
    var lastMessage = getLastMessage();
    if (lastMessage.isPending()) {
      lastMessage.cancel();
      registerEvent(
          new ChatMessageCancelledEvent(this.id, lastMessage.getId(), lastMessage.getTimestamp()));
    }

    var message = Message.userMessage(content);
    messages.add(message);

    var assistantMessage = Message.assistantMessage();
    messages.add(assistantMessage);

    registerEvent(
        new ChatMessageAddedEvent(
            this.id,
            MessageType.USER,
            message.getContent().orElseThrow(),
            message.getTimestamp(),
            assistantMessage.getId()));

    return message;
  }

  private Message getLastMessage() {
    return messages.get(messages.size() - 1);
  }

  public void editUserMessage(MessageId messageId, String content) {
    var message = getMessage(messageId, MessageType.USER);
    message.edit(content);

    var messageIndex = messages.indexOf(message);

    messages.subList(messageIndex + 1, messages.size()).clear();

    var assistantMessage = Message.assistantMessage();
    messages.add(assistantMessage);

    registerEvent(
        new ChatMessageEditedEvent(
            getId(),
            message.getId(),
            message.getContent().orElseThrow(),
            message.getTimestamp(),
            assistantMessage.getId()));
  }

  public void retryAssistantMessage(MessageId messageId) {
    var message = getMessage(messageId, MessageType.ASSISTANT);

    var messageIndex = messages.indexOf(message);

    messages.subList(messageIndex, messages.size()).clear();

    var assistantMessage = Message.assistantMessage();
    messages.add(assistantMessage);

    registerEvent(
        new AssistantMessageRetriedEvent(
            getId(), message.getId(), assistantMessage.getId(), assistantMessage.getTimestamp()));
  }

  public void addPendingToolCalls(MessageId messageId, List<ToolCall> toolCalls) {
    var message = getMessage(messageId, MessageType.ASSISTANT);

    message.addPendingToolCalls(toolCalls);

    registerEvent(new PendingToolCallsAddedEvent(getId(), Collections.unmodifiableList(toolCalls)));
  }

  public void approveToolCall(MessageId messageId, ToolCallId toolCallId) {
    var message = getMessage(messageId, MessageType.ASSISTANT);

    message.approveToolCall(toolCallId);

    registerEvent(new ToolCallApprovedEvent(getId(), messageId, toolCallId));
  }

  public void rejectToolCall(MessageId messageId, ToolCallId toolCallId) {
    var message = getMessage(messageId, MessageType.ASSISTANT);

    message.rejectToolCall(toolCallId);

    registerEvent(new ToolCallRejectedEvent(getId(), messageId, toolCallId));
  }

  public void completeMessage(MessageId messageId, String content) {
    var message = getMessage(messageId, MessageType.ASSISTANT);

    message.complete(content);

    registerEvent(new ChatMessageCompletedEvent(this.getId(), message.getId(), content));
  }

  private Message getMessage(MessageId messageId, MessageType messageType) {
    return messages.stream()
        .filter(m -> m.getId().equals(messageId) && m.getType().equals(messageType))
        .findFirst()
        .orElseThrow(() -> new NotFoundException(messageId));
  }

  public List<Message> getMessages() {
    return Collections.unmodifiableList(messages);
  }

  public static Chat start(String content) {
    var chat = new Chat(ChatId.create(), new ArrayList<>());
    chat.messages.add(Message.userMessage(content));

    var assistantMessage = Message.assistantMessage();
    chat.messages.add(assistantMessage);

    chat.registerEvent(
        new ChatStartedEvent(chat.getId(), chat.getCreatedAt(), content, assistantMessage.getId()));

    return chat;
  }
}
