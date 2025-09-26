package lab.mops.ai.infrastructure.llm;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lab.mops.ai.application.chat.CompletionService;
import lab.mops.ai.domain.chat.Message;
import lab.mops.core.api.ai.BudgetTools;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.stereotype.Component;

@Component
class SpringChatClient implements CompletionService {

  private final ToolCallingManager toolCallingManager;

  private final ChatModel chatModel;

  private final ChatOptions chatOptions;

  SpringChatClient(ChatModel chatModel, BudgetTools budgetTools) {
    this.toolCallingManager = ToolCallingManager.builder().build();
    this.chatModel = chatModel;
    this.chatOptions =
        ToolCallingChatOptions.builder()
            .toolCallbacks(ToolCallbacks.from(budgetTools))
            .internalToolExecutionEnabled(false)
            .build();
  }

  public String getResponse(List<Message> messages) {
    var prompt = buildPrompt(messages);

    var response = chatModel.call(prompt);
    while (response.hasToolCalls()) {
      var toolExecutionResult = toolCallingManager.executeToolCalls(prompt, response);

      prompt = new Prompt(toolExecutionResult.conversationHistory(), chatOptions);

      response = chatModel.call(prompt);
    }

    return response.getResult().getOutput().getText();
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
                switch (m.getType()) {
                  case USER -> new UserMessage(m.getContent().orElseThrow());
                  case ASSISTANT -> new AssistantMessage(m.getContent().orElseThrow());
                })
        .collect(Collectors.toList());
  }
}
