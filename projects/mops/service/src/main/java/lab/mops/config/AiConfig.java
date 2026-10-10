package lab.mops.config;

import lab.mops.core.api.ai.BudgetTools;
import lab.springai.tools.DateTimeTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

  @Bean
  public ToolCallbackProvider budgetTools(BudgetTools budgetTools) {
    return MethodToolCallbackProvider.builder().toolObjects(budgetTools).build();
  }

  @Bean
  public DateTimeTools dateTimeTools() {
    return new DateTimeTools();
  }
}
