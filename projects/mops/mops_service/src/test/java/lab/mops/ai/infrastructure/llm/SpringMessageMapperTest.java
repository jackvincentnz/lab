package lab.mops.ai.infrastructure.llm;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;
import lab.mops.ai.application.chat.completions.AssistantMessage;
import lab.mops.ai.application.chat.completions.ToolCall;
import lab.mops.ai.application.chat.completions.ToolResultMessage;
import lab.mops.ai.application.chat.completions.UserMessage;
import lab.mops.core.api.ai.BudgetTools;
import lab.mops.core.application.budget.data.ResolvedBudget;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.tool.annotation.Tool;

class SpringMessageMapperTest extends TestBase {

  SpringMessageMapper messageMapper = new SpringMessageMapper();

  @Test
  void map_mapsUserMessage() {
    var content = randomString();
    var userMessage = new UserMessage(content);

    var result = messageMapper.map(List.of(userMessage));

    var mappedMessage = (org.springframework.ai.chat.messages.UserMessage) result.get(0);
    assertThat(mappedMessage.getText()).isEqualTo(content);
  }

  @Test
  void map_mapsAssistantMessage() {
    var content = randomString();
    var toolId = randomString();
    var toolName = randomString();
    var toolArguments = randomString();
    var toolCalls = new ToolCall(toolId, toolName, toolArguments);
    var assistantMessage = AssistantMessage.of(content, List.of(toolCalls));

    var result = messageMapper.map(List.of(assistantMessage));

    var mappedMessage = (org.springframework.ai.chat.messages.AssistantMessage) result.get(0);
    assertThat(mappedMessage.getText()).isEqualTo(content);
    var mappedToolCall = mappedMessage.getToolCalls().get(0);
    assertThat(mappedToolCall.id()).isEqualTo(toolId);
    assertThat(mappedToolCall.name()).isEqualTo(toolName);
    assertThat(mappedToolCall.arguments()).isEqualTo(toolArguments);
  }

  @Test
  void map_mapsToolResultMessage() {
    var content = randomString();
    var toolCallId = randomString();
    var toolName = randomString();
    var toolResultMessage = new ToolResultMessage(content, toolCallId, toolName);

    var result = messageMapper.map(List.of(toolResultMessage));

    var mappedMessage = (org.springframework.ai.chat.messages.ToolResponseMessage) result.get(0);
    var mappedToolResponse = mappedMessage.getResponses().get(0);
    assertThat(mappedToolResponse.id()).isEqualTo(toolCallId);
    assertThat(mappedToolResponse.name()).isEqualTo(toolName);
    assertThat(mappedToolResponse.responseData()).isEqualTo(content);
  }

  @Test
  void map_mapsChatResponse() {
    var content = randomString();
    var id = randomString();
    var toolName = randomString();
    var arguments = randomString();
    var chatResponse =
        org.springframework.ai.chat.model.ChatResponse.builder()
            .generations(
                List.of(
                    new Generation(
                        org.springframework.ai.chat.messages.AssistantMessage.builder()
                            .content(content)
                            .toolCalls(
                                List.of(
                                    new org.springframework.ai.chat.messages.AssistantMessage
                                        .ToolCall(id, randomString(), toolName, arguments)))
                            .build())))
            .build();

    var result = messageMapper.map(chatResponse);

    assertThat(result.getContent()).hasValue(content);
    var toolCall = result.getToolCalls().iterator().next();
    assertThat(toolCall.id()).isEqualTo(id);
    assertThat(toolCall.toolName()).isEqualTo(toolName);
    assertThat(toolCall.arguments()).isEqualTo(arguments);
  }

  static class TestTools implements BudgetTools {
    @Tool
    @Override
    public void createBudget(String name) {}

    @Tool
    @Override
    public Collection<ResolvedBudget> getAllBudgets() {
      return List.of();
    }
  }
}
