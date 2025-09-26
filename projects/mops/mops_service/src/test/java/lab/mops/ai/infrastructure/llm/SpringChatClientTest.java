package lab.mops.ai.infrastructure.llm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import lab.mops.ai.domain.chat.Chat;
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

  SpringChatClient springChatClient;

  @BeforeEach
  void setup() {
    springChatClient = new SpringChatClient(chatModel, new TestTools());
  }

  @Test
  void getResponse_returnsResponse() {
    var content = randomString();
    var chat = Chat.start(content);
    var assistantContent = randomString();
    var chatResponse =
        ChatResponse.builder()
            .generations(List.of(new Generation(new AssistantMessage(assistantContent))))
            .build();

    when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

    var response = springChatClient.getResponse(List.of(chat.getMessages().get(0)));

    assertThat(response).isEqualTo(assistantContent);
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
