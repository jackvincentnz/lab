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

  private final SpringMessageMapper messageMapper;

  SpringChatClient(
      ChatModel chatModel, BudgetTools budgetTools, SpringMessageMapper messageMapper) {
    this.chatModel = chatModel;
    this.budgetTools = budgetTools;
    this.chatOptions =
        ToolCallingChatOptions.builder()
            .toolCallbacks(ToolCallbacks.from(budgetTools))
            .internalToolExecutionEnabled(false)
            .build();
    this.messageMapper = messageMapper;
  }

  @Override
  public AssistantMessage getResponse(List<Message> messages) {
    var prompt = buildPrompt(messages);

    var response = chatModel.call(prompt);

    return messageMapper.map(response);
  }

  private Prompt buildPrompt(List<Message> messages) {
    var allMessages = new ArrayList<>(messageMapper.map(messages));
    allMessages.add(0, new SystemMessage(Constants.SYSTEM_PROMPT));

    return new Prompt(allMessages, chatOptions);
  }

  @Override
  public List<Tool> getTools() {
    return Arrays.stream(ToolCallbacks.from(budgetTools))
        .map(SpringTool::new)
        .collect(Collectors.toUnmodifiableList());
  }
}
