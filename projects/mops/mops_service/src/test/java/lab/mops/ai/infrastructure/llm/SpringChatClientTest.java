package lab.mops.ai.infrastructure.llm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import lab.mops.ai.application.chat.completions.UserMessage;
import lab.mops.core.api.ai.BudgetTools;
import lab.mops.core.application.budget.data.ResolvedBudget;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.tool.annotation.Tool;

@ExtendWith(MockitoExtension.class)
class SpringChatClientTest extends TestBase {

  @Mock ChatModel chatModel;

  @Mock SpringMessageMapper messageMapper;

  SpringChatClient springChatClient;

  @BeforeEach
  void setup() {
    springChatClient = new SpringChatClient(chatModel, new TestTools(), messageMapper);
  }

  @Test
  void getResponse_returnsMappedResponse() {
    var content = randomString();
    var userMessage = new UserMessage(content);
    var assistantContent = randomString();

    var chatResponse =
        ChatResponse.builder()
            .generations(List.of(new Generation(new AssistantMessage(assistantContent))))
            .build();

    when(messageMapper.map(anyList())).thenReturn(List.of());
    when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
    when(messageMapper.map(chatResponse))
        .thenReturn(lab.mops.ai.application.chat.completions.AssistantMessage.of(assistantContent));

    var response = springChatClient.getResponse(List.of(userMessage));

    assertThat(response.getContent()).hasValue(assistantContent);
  }

  @Test
  void getTools_returnsAvailableTools() {
    var tools =
        springChatClient.getTools().stream().map(t -> t.getToolDefinition().name()).toList();

    assertThat(tools).hasSize(2);
    Arrays.stream(TestTools.class.getMethods())
        .filter(method -> method.isAnnotationPresent(Tool.class))
        .map(Method::getName)
        .forEach(
            name -> {
              assertThat(tools).contains(name);
            });
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
