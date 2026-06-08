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
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.stereotype.Component;

@Component
class SpringChatClient implements CompletionService, ToolProvider {

  private final ChatModel chatModel;

  private final ChatOptions chatOptions;

  private final BudgetTools budgetTools;

  private final SpringMessageMapper messageMapper;

  private final ToolApprovalPolicy approvalPolicy;

  SpringChatClient(
      ChatModel chatModel,
      BudgetTools budgetTools,
      SpringMessageMapper messageMapper,
      ToolApprovalPolicy approvalPolicy) {
    this.chatModel = chatModel;
    this.budgetTools = budgetTools;
    this.chatOptions =
        ToolCallingChatOptions.builder()
            .toolCallbacks(
                Arrays.stream(ToolCallbacks.from(budgetTools))
                    .map(NonExecutingToolCallback::new)
                    .toArray(ToolCallback[]::new))
            .build();
    this.messageMapper = messageMapper;
    this.approvalPolicy = approvalPolicy;
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
        .map(cb -> new SpringTool(cb, approvalPolicy))
        .collect(Collectors.toUnmodifiableList());
  }

  private record NonExecutingToolCallback(ToolCallback delegate) implements ToolCallback {
    @Override
    public ToolDefinition getToolDefinition() {
      return delegate.getToolDefinition();
    }

    @Override
    public ToolMetadata getToolMetadata() {
      return delegate.getToolMetadata();
    }

    @Override
    public String call(String toolInput) {
      throw new IllegalStateException("Tool calls must be executed by the MOPS approval loop");
    }

    @Override
    public String call(String toolInput, ToolContext toolContext) {
      throw new IllegalStateException("Tool calls must be executed by the MOPS approval loop");
    }
  }
}
