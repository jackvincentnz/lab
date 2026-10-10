package lab.mops.ai.infrastructure.llm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import lab.mops.ai.application.chat.completions.UserMessage;
import lab.mops.core.api.ai.BudgetTools;
import lab.mops.core.application.budget.data.ResolvedBudget;
import lab.test.TestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

@ExtendWith(MockitoExtension.class)
class SpringChatClientTest extends TestBase {

  @Mock ChatModel chatModel;

  @Mock SpringMessageMapper messageMapper;

  @Mock ToolApprovalPolicy approvalPolicy;

  SpringChatClient springChatClient;

  @BeforeEach
  void setup() {
    springChatClient =
        new SpringChatClient(
            chatModel, new TestTools(), messageMapper, approvalPolicy, systemPrompt());
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
  void getResponse_registersToolsWithoutAllowingSpringAiToExecuteThem() {
    var chatResponse =
        ChatResponse.builder()
            .generations(List.of(new Generation(new AssistantMessage(randomString()))))
            .build();
    var promptCaptor = ArgumentCaptor.forClass(Prompt.class);

    when(messageMapper.map(anyList())).thenReturn(List.of());
    when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
    when(messageMapper.map(chatResponse))
        .thenReturn(lab.mops.ai.application.chat.completions.AssistantMessage.of(randomString()));

    springChatClient.getResponse(List.of(new UserMessage(randomString())));

    verify(chatModel).call(promptCaptor.capture());
    var options = (ToolCallingChatOptions) promptCaptor.getValue().getOptions();
    var toolCallback = options.getToolCallbacks().get(0);

    assertThat(toolCallback.getToolDefinition().name()).isNotBlank();
    assertThatThrownBy(() -> toolCallback.call("{}"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Tool calls must be executed by the MOPS approval loop");
  }

  @Test
  void getResponse_preservesTheModelsOwnOptionsType() {
    when(chatModel.getOptions())
        .thenReturn(GoogleGenAiChatOptions.builder().model("gemini-test").build());
    springChatClient =
        new SpringChatClient(
            chatModel, new TestTools(), messageMapper, approvalPolicy, systemPrompt());

    var chatResponse =
        ChatResponse.builder()
            .generations(List.of(new Generation(new AssistantMessage(randomString()))))
            .build();
    var promptCaptor = ArgumentCaptor.forClass(Prompt.class);

    when(messageMapper.map(anyList())).thenReturn(List.of());
    when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
    when(messageMapper.map(chatResponse))
        .thenReturn(lab.mops.ai.application.chat.completions.AssistantMessage.of(randomString()));

    springChatClient.getResponse(List.of(new UserMessage(randomString())));

    verify(chatModel).call(promptCaptor.capture());
    var options = promptCaptor.getValue().getOptions();

    assertThat(options).isInstanceOf(GoogleGenAiChatOptions.class);
    assertThat(options.getModel()).isEqualTo("gemini-test");
    assertThat(((GoogleGenAiChatOptions) options).getToolCallbacks()).hasSize(2);
  }

  @Test
  void getResponse_prependsTheSystemPromptFromTheClasspath() {
    var chatResponse =
        ChatResponse.builder()
            .generations(List.of(new Generation(new AssistantMessage(randomString()))))
            .build();
    var promptCaptor = ArgumentCaptor.forClass(Prompt.class);

    when(messageMapper.map(anyList())).thenReturn(List.of());
    when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
    when(messageMapper.map(chatResponse))
        .thenReturn(lab.mops.ai.application.chat.completions.AssistantMessage.of(randomString()));

    springChatClient.getResponse(List.of(new UserMessage(randomString())));

    verify(chatModel).call(promptCaptor.capture());
    var systemMessage = promptCaptor.getValue().getInstructions().get(0);

    assertThat(systemMessage).isInstanceOf(SystemMessage.class);
    assertThat(systemMessage.getText())
        .startsWith("You are a skilled expert in planning and budgeting for marketing campaigns.\n")
        .contains("type QuarterlyTotal {\n  quarter: Quarter!\n  fiscalYear: Int!\n")
        .contains("4. **Call the tools.**")
        .endsWith("6. **Ask the user if they need anything else.**\n");
  }

  @Test
  void constructor_missingSystemPrompt_failsFast() {
    var missing = new ClassPathResource(randomString());

    assertThatThrownBy(
            () ->
                new SpringChatClient(
                    chatModel, new TestTools(), messageMapper, approvalPolicy, missing))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageStartingWith("System prompt resource not found: ");
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

  private static Resource systemPrompt() {
    return new DefaultResourceLoader().getResource(SpringChatClient.SYSTEM_PROMPT_LOCATION);
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
