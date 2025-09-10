package lab.mops.ai.infrastructure.llm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import lab.mops.ai.domain.chat.Chat;
import lab.mops.core.api.ai.BudgetTools;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

@ExtendWith(MockitoExtension.class)
class SpringChatClientTest extends TestBase {

  @Mock ChatClient.Builder chatClientBuilder;

  @Mock ChatClient chatClient;

  @Mock BudgetTools budgetTools;

  SpringChatClient springChatClient;

  @BeforeEach
  void setUp() {
    when(chatClientBuilder.build()).thenReturn(chatClient);
    springChatClient = new SpringChatClient(chatClientBuilder, budgetTools);
  }

  @Test
  void getResponse_returnsResponse() {
    var userPrompt = randomString();
    var chat = Chat.start(userPrompt);
    var expectedResponse = randomString();

    var requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
    var responseSpec = mock(ChatClient.CallResponseSpec.class);

    when(chatClient.prompt()).thenReturn(requestSpec);
    when(requestSpec.system(any(String.class))).thenReturn(requestSpec);
    when(requestSpec.messages(any(List.class))).thenReturn(requestSpec);
    when(requestSpec.tools(budgetTools)).thenReturn(requestSpec);
    when(requestSpec.call()).thenReturn(responseSpec);
    when(responseSpec.content()).thenReturn(expectedResponse);

    var response = springChatClient.getResponse(List.of(chat.getMessages().get(0)));

    assertThat(response).isEqualTo(expectedResponse);
  }
}
