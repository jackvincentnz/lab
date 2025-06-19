package nz.geek.jack.mops.ai.adapter.api.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.function.Consumer;
import nz.geek.jack.mops.core.api.ai.BudgetTools;
import nz.geek.jack.springai.tools.DateTimeTools;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;

@ExtendWith(MockitoExtension.class)
class AiControllerTest extends TestBase {

  @Mock ChatClient.Builder chatClientBuilder;

  @Mock ChatMemory chatMemory;

  @Mock ChatClient chatClient;

  @Mock BudgetTools budgetTools;

  AiController aiController;

  @BeforeEach
  void setUp() {
    when(chatClientBuilder.defaultAdvisors(any(Advisor.class))).thenReturn(chatClientBuilder);
    when(chatClientBuilder.build()).thenReturn(chatClient);
    aiController = new AiController(chatClientBuilder, chatMemory, budgetTools);
  }

  @Test
  void chat_returnsResponse() {
    var chatId = randomString();
    var prompt = randomString();
    var expectedResponse = randomString();

    var requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
    var responseSpec = mock(ChatClient.CallResponseSpec.class);

    when(chatClient.prompt()).thenReturn(requestSpec);
    when(requestSpec.user(prompt)).thenReturn(requestSpec);
    when(requestSpec.advisors(any(Consumer.class))).thenReturn(requestSpec);
    when(requestSpec.tools(any(DateTimeTools.class), eq(budgetTools))).thenReturn(requestSpec);
    when(requestSpec.call()).thenReturn(responseSpec);
    when(responseSpec.content()).thenReturn(expectedResponse);

    ChatResponse response = aiController.chat(chatId, new ChatRequest(prompt));

    assertThat(response.message()).isEqualTo(expectedResponse);
  }
}
