package lab.mops.ai.infrastructure.llm;

import java.util.List;
import java.util.stream.Collectors;
import lab.mops.ai.application.chat.CompletionService;
import lab.mops.ai.domain.chat.Message;
import lab.mops.core.api.ai.BudgetTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

@Component
class SpringChatClient implements CompletionService {

  private final ChatClient chatClient;

  private final BudgetTools budgetTools;

  SpringChatClient(ChatClient.Builder chatClientBuilder, BudgetTools budgetTools) {
    this.chatClient = chatClientBuilder.build();
    this.budgetTools = budgetTools;
  }

  public String getResponse(List<Message> messages) {
    return this.chatClient
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
  spendTotals: SpendTotals!
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

type SpendTotals {
  monthlyTotals: [MonthlyTotal!]!
  quarterlyTotals: [QuarterlyTotal!]!
  annualTotals: [AnnualTotal!]!
  grandTotal: BigDecimal!
}

type MonthlyTotal {
  month: Month!
  year: Int!
  total: BigDecimal!
}

enum Month {
  JANUARY
  FEBRUARY
  MARCH
  APRIL
  MAY
  JUNE
  JULY
  AUGUST
  SEPTEMBER
  OCTOBER
  NOVEMBER
  DECEMBER
}

type QuarterlyTotal {
  quarter: Quarter!
  fiscalYear: Int!
  total: BigDecimal!
}

enum Quarter {
  Q1
  Q2
  Q3
  Q4
}

type AnnualTotal {
  year: Int!
  total: BigDecimal!
}

Your general process is as follows:

1. **Understand the user's request.** Analyze the user's initial request to understand the goal - for example, "I want to plan a budget for Q3 2025, North America - Demand Gen?" If you do not understand the request, ask for more information.
2. **Identify the appropriate tools.** You will be provided with tools for a marketing planning saas application (create, and get all budgets). Identify one **or more** appropriate tools to accomplish the user's request.
3. **Populate and validate the parameters.** Before calling the tools, do some reasoning to make sure that you are populating the tool parameters correctly. For example, when creating a new budget, make sure that the name field is set and aligns to the user's request.
4. **Call the tools.** Once the parameters are validated, call the tool with the determined parameters.
5. **Analyze the tools' results, and provide insights back to the user.** Return the tools' result in a human-readable markdown format. If your result contains multiple items, always use a markdown table to report back.
6. **Ask the user if they need anything else.**
""")
        .messages(
            messages.stream()
                .map(
                    m ->
                        switch (m.getType()) {
                          case USER -> new UserMessage(m.getContent().orElseThrow());
                          case ASSISTANT -> new AssistantMessage(m.getContent().orElseThrow());
                        })
                .collect(Collectors.toList()))
        .tools(budgetTools)
        .call()
        .content();
  }
}
