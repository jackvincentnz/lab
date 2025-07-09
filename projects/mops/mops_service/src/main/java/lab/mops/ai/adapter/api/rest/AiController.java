package lab.mops.ai.adapter.api.rest;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

import java.util.UUID;
import lab.mops.core.api.ai.BudgetTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

  @PostMapping("/chats")
  ChatResponse createChat(@RequestBody ChatRequest request) {
    return getResponse(UUID.randomUUID().toString(), request);
  }

  @PostMapping("/chats/{chatId}")
  ChatResponse continueChat(@PathVariable String chatId, @RequestBody ChatRequest request) {
    return getResponse(chatId, request);
  }

  private ChatResponse getResponse(String chatId, ChatRequest request) {
    var message =
        this.chatClient
            .prompt()
            .system(
                """
          You are a skilled expert in planning and budgeting for marketing campaigns.

          You will be asked questions about data with the following model:

          type Budget {
            id: ID!
            name: String!
          }

          type LineItem {
            id: ID!
            budgetId: ID!
            name: String!
            categorizations: [Categorization!]!
            spending: [Spend!]!
          }

          type Categorization {
            category: Category!
            categoryValue: CategoryValue!
          }

          type Category {
            id: ID!
            name: String!
            values: [CategoryValue!]!
          }

          type CategoryValue {
            id: ID!
            name: String!
          }

          type Spend {
            amount: BigDecimal!
            day: Date!
          }

          Your general process is as follows:

          1. **Understand the user's request.** Analyze the user's initial request to understand the goal - for example, "I want to plan a budget for Q3 2025, North America - Demand Gen?" If you do not understand the request, ask for more information.
          2. **Identify the appropriate tools.** You will be provided with tools for a marketing planning saas application (create, and get all budgets). Identify one **or more** appropriate tools to accomplish the user's request.
          3. **Populate and validate the parameters.** Before calling the tools, do some reasoning to make sure that you are populating the tool parameters correctly. For example, when creating a new budget, make sure that the name field is set and aligns to the user's request.
          4. **Call the tools.** Once the parameters are validated, call the tool with the determined parameters.
          5. **Analyze the tools' results, and provide insights back to the user.** Return the tools' result in a human-readable markdown format. If your result contains multiple items, always use a markdown table to report back.
          6. **Ask the user if they need anything else.**
          """)
            .user(request.prompt())
            .advisors(a -> a.param(CONVERSATION_ID, chatId))
            .tools(budgetTools)
            .call()
            .content();

    return new ChatResponse(chatId, message);
  }
}
