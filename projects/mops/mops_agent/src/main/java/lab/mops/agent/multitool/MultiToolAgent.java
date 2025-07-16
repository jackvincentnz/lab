package lab.mops.agent.multitool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.tools.BaseTool;
import com.google.adk.tools.mcp.McpToolset;
import com.google.adk.tools.mcp.SseServerParameters;
import java.util.ArrayList;
import java.util.logging.Logger;

public class MultiToolAgent {

  private static final Logger logger = Logger.getLogger(MultiToolAgent.class.getName());

  // --- Define Constants ---
  private static final String MODEL_NAME = "gemini-2.5-flash";

  // ROOT_AGENT needed for ADK Web UI.
  public static BaseAgent ROOT_AGENT = initAgent();

  public static BaseAgent initAgent() {
    // Set up MCP Toolbox connection to Cloud SQL
    try {
      var mcpServerUrl = System.getenv("MCP_SERVER_URL");
      if (mcpServerUrl == null || mcpServerUrl.isEmpty()) {
        mcpServerUrl = "http://127.0.0.1:8080/sse"; // Fallback URL
        logger.warning(
            "⚠️ MCP_SERVER_URL environment variable not set, using default:" + mcpServerUrl);
      }

      var params = SseServerParameters.builder().url(mcpServerUrl).build();
      logger.info("🕰️ Initializing MCP toolset with params" + params);

      var result = McpToolset.fromServer(params, new ObjectMapper()).get();
      logger.info("⭐ MCP Toolset initialized: " + result.toString());
      if (result.getTools() != null && !result.getTools().isEmpty()) {
        logger.info("⭐ MCP Tools loaded: " + result.getTools().size());
      }
      var mcpTools = result.getTools().stream().map(mcpTool -> (BaseTool) mcpTool).toList();
      logger.info("🛠️ MCP TOOLS: " + mcpTools.toString());

      var allTools = new ArrayList<>(mcpTools);
      logger.info("🌈 ALL TOOLS: " + allTools.toString());
      return LlmAgent.builder()
          .model(MODEL_NAME)
          .name("MarketingOperationsAssistant")
          .description("Helps marketers plan, budget, execute, and analyze in marketing campaigns")
          .instruction(
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
          .tools(allTools)
          .outputKey("mops_assistant_result")
          .build();
    } catch (Exception e) {
      logger.info("Error initializing MCP toolset and starting agent " + e.getMessage());
      return null;
    }
  }
}
