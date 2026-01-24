package lab.mops.ai.functional;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.HttpServletResponse;
import lab.mops.api.gql.types.ChatMessageStatus;
import lab.mops.api.gql.types.ChatMessageType;
import lab.mops.client.TestClient;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ChatFunctionalTest extends TestBase {

  @Autowired TestClient client;

  @Test
  void startChat_startsChat() {
    var content = randomString();

    var response = client.startChat(content);

    assertThat(response.getSuccess()).isTrue();
    assertThat(response.getCode()).isEqualTo(HttpServletResponse.SC_CREATED);
    assertThat(response.getMessage()).isNotBlank();
    assertThat(response.getChat().getId()).isNotBlank();
    assertThat(response.getChat().getCreatedAt()).isNotBlank();
    assertThat(response.getChat().getUpdatedAt()).isNotBlank();

    var messages = response.getChat().getMessages();
    assertThat(messages).hasSize(2);
    assertThat(messages.get(0).getContent()).isEqualTo(content);
    assertThat(messages.get(0).getId()).isNotBlank();
    assertThat(messages.get(0).getCreatedAt()).isNotBlank();
    assertThat(messages.get(0).getUpdatedAt()).isNotBlank();
    assertThat(messages.get(1).getId()).isNotBlank();
    assertThat(messages.get(1).getType()).isEqualTo(ChatMessageType.ASSISTANT);
    assertThat(messages.get(1).getStatus()).isEqualTo(ChatMessageStatus.PENDING);
    assertThat(messages.get(1).getCreatedAt()).isNotBlank();
    assertThat(messages.get(1).getUpdatedAt()).isNotBlank();
  }

  @Test
  void chat_returnsAllFields() {
    var content = randomString();

    var chatId = client.startChat(content).getChat().getId();

    var chat = client.chat(chatId);

    assertThat(chat.getId()).isNotBlank();
    assertThat(chat.getCreatedAt()).isNotBlank();
    assertThat(chat.getUpdatedAt()).isNotBlank();

    var messages = chat.getMessages();
    assertThat(messages).hasSize(2);
    assertThat(messages.get(0).getContent()).isEqualTo(content);
    assertThat(messages.get(0).getId()).isNotBlank();
    assertThat(messages.get(0).getCreatedAt()).isNotBlank();
    assertThat(messages.get(0).getUpdatedAt()).isNotBlank();
    assertThat(messages.get(1).getId()).isNotBlank();
    assertThat(messages.get(1).getType()).isEqualTo(ChatMessageType.ASSISTANT);
    assertThat(messages.get(1).getStatus()).isEqualTo(ChatMessageStatus.PENDING);
    assertThat(messages.get(1).getCreatedAt()).isNotBlank();
    assertThat(messages.get(1).getUpdatedAt()).isNotBlank();
  }

  @Test
  void allChats_returnsAllChats() {
    var content = randomString();
    client.startChat(content).getChat().getId();

    var chats = client.allChats();

    assertThat(chats.size()).isEqualTo(1);

    var chat = chats.get(0);
    assertThat(chat.getId()).isNotBlank();
    assertThat(chat.getCreatedAt()).isNotBlank();
    assertThat(chat.getUpdatedAt()).isNotBlank();

    var messages = chat.getMessages();
    assertThat(messages).hasSize(2);
    assertThat(messages.get(0).getContent()).isEqualTo(content);
    assertThat(messages.get(0).getId()).isNotBlank();
    assertThat(messages.get(0).getCreatedAt()).isNotBlank();
    assertThat(messages.get(0).getUpdatedAt()).isNotBlank();
    assertThat(messages.get(1).getId()).isNotBlank();
    assertThat(messages.get(1).getType()).isEqualTo(ChatMessageType.ASSISTANT);
    assertThat(messages.get(1).getStatus()).isEqualTo(ChatMessageStatus.PENDING);
    assertThat(messages.get(1).getCreatedAt()).isNotBlank();
    assertThat(messages.get(1).getUpdatedAt()).isNotBlank();
  }

  @Test
  void addUserMessage_addsMessage() {
    var content = randomString();
    var chatId = client.startChat(content).getChat().getId();

    var response = client.addUserMessage(chatId, content);

    assertThat(response.getSuccess()).isTrue();
    assertThat(response.getCode()).isEqualTo(HttpServletResponse.SC_OK);
    assertThat(response.getMessage()).isNotBlank();

    var messages = response.getChat().getMessages();
    assertThat(messages).hasSize(4);
    assertThat(messages.get(2).getContent()).isEqualTo(content);
    assertThat(messages.get(2).getId()).isNotBlank();
    assertThat(messages.get(2).getCreatedAt()).isNotBlank();
    assertThat(messages.get(2).getUpdatedAt()).isNotBlank();
    assertThat(messages.get(3).getId()).isNotBlank();
    assertThat(messages.get(3).getType()).isEqualTo(ChatMessageType.ASSISTANT);
    assertThat(messages.get(3).getStatus()).isEqualTo(ChatMessageStatus.PENDING);
    assertThat(messages.get(3).getCreatedAt()).isNotBlank();
    assertThat(messages.get(3).getUpdatedAt()).isNotBlank();
  }

  @Test
  void addUserMessage_editsMessage() {
    var content = randomString();
    var chat = client.startChat(randomString()).getChat();
    var messageId = chat.getMessages().get(0).getId();

    var response = client.editUserMessage(chat.getId(), messageId, content);

    assertThat(response.getSuccess()).isTrue();
    assertThat(response.getCode()).isEqualTo(HttpServletResponse.SC_OK);
    assertThat(response.getMessage()).isNotBlank();

    var messages = response.getChat().getMessages();
    assertThat(messages).hasSize(2);
    assertThat(messages.get(0).getContent()).isEqualTo(content);
  }

  @Test
  void retryAssistantMessage_retriesMessage() {
    var chat = client.startChat(randomString()).getChat();
    var messageId = chat.getMessages().get(1).getId();

    var response = client.retryAssistantMessage(chat.getId(), messageId);

    assertThat(response.getSuccess()).isTrue();
    assertThat(response.getCode()).isEqualTo(HttpServletResponse.SC_OK);
    assertThat(response.getMessage()).isNotBlank();

    var messages = response.getChat().getMessages();
    assertThat(messages).hasSize(2);
    assertThat(messages.get(1).getId()).isNotEqualTo(messageId);
    assertThat(messages.get(1).getStatus()).isEqualTo(ChatMessageStatus.PENDING);
    assertThat(messages.get(1).getType()).isEqualTo(ChatMessageType.ASSISTANT);
  }
}
