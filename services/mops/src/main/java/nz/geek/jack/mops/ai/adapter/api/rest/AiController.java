package nz.geek.jack.mops.ai.adapter.api.rest;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

import nz.geek.jack.mops.core.api.ai.BudgetTools;
import nz.geek.jack.springai.tools.DateTimeTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
class AiController {

  private final ChatClient chatClient;

  private final BudgetTools budgetTools;

  public AiController(
      ChatClient.Builder chatClientBuilder, ChatMemory chatMemory, BudgetTools budgetTools) {
    this.chatClient =
        chatClientBuilder
            .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
            .build();
    this.budgetTools = budgetTools;
  }

  @GetMapping("/chat/{chatId}")
  String chat(@PathVariable String chatId, String prompt) {
    return this.chatClient
        .prompt()
        .user(prompt)
        .advisors(a -> a.param(CONVERSATION_ID, chatId))
        .tools(new DateTimeTools(), budgetTools)
        .call()
        .content();
  }
}
