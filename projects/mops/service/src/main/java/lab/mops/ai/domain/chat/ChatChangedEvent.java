package lab.mops.ai.domain.chat;

/** A saved change to the state visible to chat subscribers. */
public interface ChatChangedEvent {
  ChatId chatId();
}
