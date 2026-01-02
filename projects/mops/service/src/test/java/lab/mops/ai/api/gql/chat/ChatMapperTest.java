package lab.mops.ai.api.gql.chat;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import lab.mops.ai.domain.chat.Chat;
import lab.mops.ai.domain.chat.ToolCall;
import lab.mops.ai.domain.chat.ToolCallId;
import lab.mops.ai.domain.chat.ToolCallStatus;
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

    assertThat(result.getMessages()).hasSize(2);
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

  @Test
  void map_mapsPendingApprovalToolCalls() {
    var domainChat = Chat.start(randomString());
    var assistantMessage = domainChat.getMessages().get(1);
    var toolCallId = ToolCallId.create();
    var toolCallName = randomString();
    var toolCallArguments = randomString();
    var toolCallStatus = ToolCallStatus.PENDING_APPROVAL;

    domainChat.addPendingToolCalls(
        assistantMessage.getId(),
        List.of(ToolCall.of(toolCallId, toolCallName, toolCallArguments, toolCallStatus)));

    var result = chatMapper.map(domainChat);

    var mappedMessage = result.getMessages().get(1);
    assertThat(mappedMessage.getToolCalls()).hasSize(1);
    var mappedToolCall = mappedMessage.getToolCalls().get(0);
    assertThat(mappedToolCall.getId()).isEqualTo(toolCallId.toString());
    assertThat(mappedToolCall.getName()).isEqualTo(toolCallName);
    assertThat(mappedToolCall.getArguments()).isEqualTo(toolCallArguments);
    assertThat(mappedToolCall.getStatus())
        .isEqualTo(lab.mops.api.gql.types.ToolCallStatus.PENDING_APPROVAL);
  }
}
