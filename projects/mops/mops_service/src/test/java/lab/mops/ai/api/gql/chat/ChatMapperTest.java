package lab.mops.ai.api.gql.chat;

import static org.assertj.core.api.Assertions.assertThat;

import lab.mops.ai.domain.chat.Chat;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;

class ChatMapperTest extends TestBase {

  ChatMapper chatMapper = new ChatMapper();

  @Test
  void map_mapsId() {
    var domainChat = Chat.start(randomString());

    var result = chatMapper.map(domainChat);

    assertThat(result.getId()).isEqualTo(domainChat.getId().toString());
  }

  @Test
  void map_mapsMessages() {
    var domainChat = Chat.start(randomString());
    var message = domainChat.getMessages().get(0);

    var result = chatMapper.map(domainChat);

    assertThat(result.getMessages()).hasSize(1);
    var mappedMessage = result.getMessages().get(0);
    assertThat(mappedMessage.getId()).isEqualTo(message.getId().toString());
    assertThat(mappedMessage.getType().name()).isEqualTo(message.getType().name());
    assertThat(mappedMessage.getStatus().name()).isEqualTo(message.getStatus().name());
    assertThat(mappedMessage.getContent()).isEqualTo(message.getContent().orElseThrow());
    // FIXME: align naming and push updatedAt down to domain
    assertThat(mappedMessage.getCreatedAt()).isEqualTo(message.getTimestamp().toString());
    assertThat(mappedMessage.getUpdatedAt()).isEqualTo(message.getTimestamp().toString());
  }

  @Test
  void map_mapsCreatedAt() {
    var domainChat = Chat.start(randomString());

    var result = chatMapper.map(domainChat);

    assertThat(result.getCreatedAt()).isEqualTo(domainChat.getCreatedAt().toString());
  }

  @Test
  void map_mapsUpdatedAt() {
    var domainChat = Chat.start(randomString());

    var result = chatMapper.map(domainChat);

    assertThat(result.getUpdatedAt()).isEqualTo(domainChat.getUpdatedAt().toString());
  }
}
