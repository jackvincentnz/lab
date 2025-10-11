package lab.mops.ai.infrastructure.llm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lab.mops.ai.application.chat.Tool;
import lab.mops.ai.application.chat.ToolProvider;
import lab.mops.ai.application.chat.completions.AssistantMessage;
import lab.mops.ai.application.chat.completions.CompletionService;
import lab.mops.ai.application.chat.completions.Message;
import lab.mops.ai.application.chat.completions.ToolCall;
import lab.mops.ai.application.chat.completions.UserMessage;
import lab.mops.core.api.ai.BudgetTools;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.stereotype.Component;

@Component
class SpringChatClient implements CompletionService, ToolProvider {

  private final ChatModel chatModel;

  private final ChatOptions chatOptions;

  private final BudgetTools budgetTools;

  SpringChatClient(ChatModel chatModel, BudgetTools budgetTools) {
    this.chatModel = chatModel;
    this.budgetTools = budgetTools;
    this.chatOptions =
        ToolCallingChatOptions.builder()
            .toolCallbacks(ToolCallbacks.from(budgetTools))
            .internalToolExecutionEnabled(false)
            .build();
  }

  @Override
  public AssistantMessage getResponse(List<Message> messages) {
    var prompt = buildPrompt(messages);

    var response = chatModel.call(prompt);

    return AssistantMessage.of(
        response.getResult().getOutput().getText(),
        response.getResult().getOutput().getToolCalls().stream()
            .map(SpringChatClient::map)
            .toList());
  }

  private static ToolCall map(
      org.springframework.ai.chat.messages.AssistantMessage.ToolCall toolCall) {
    return new ToolCall(toolCall.id(), toolCall.name(), toolCall.arguments());
  }

  private Prompt buildPrompt(List<Message> messages) {
    var allMessages = new ArrayList<>(map(messages));
    allMessages.add(0, new SystemMessage(Constants.SYSTEM_PROMPT));

    return new Prompt(allMessages, chatOptions);
  }

  private List<org.springframework.ai.chat.messages.Message> map(List<Message> messages) {
    return messages.stream()
        .map(
            m ->
                switch (m.getRole()) {
                  case USER -> map((UserMessage) m);
                  case ASSISTANT -> map((AssistantMessage) m);
                  default ->
                      throw new IllegalStateException("Unexpected message role: " + m.getRole());
                })
        .collect(Collectors.toList());
  }

  private static org.springframework.ai.chat.messages.UserMessage map(UserMessage m) {
    return new org.springframework.ai.chat.messages.UserMessage(m.content());
  }

  private static org.springframework.ai.chat.messages.AssistantMessage map(
      AssistantMessage message) {
    return new org.springframework.ai.chat.messages.AssistantMessage(
        message
            .getContent()
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "Assistant message missing content. Tool messages not supported.")));
  }

  @Override
  public List<Tool> getTools() {
    return Arrays.stream(ToolCallbacks.from(budgetTools))
        .map(SpringTool::new)
        .collect(Collectors.toUnmodifiableList());
  }
}
