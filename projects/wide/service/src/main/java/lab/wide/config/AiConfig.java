package lab.wide.config;

import lab.wide.api.RefinementDetailTools;
import lab.wide.api.RefinementTools;
import lab.wide.api.SearchTools;
import lab.wide.api.SignalTools;
import lab.wide.api.StrategyTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

  @Bean
  public ToolCallbackProvider wideToolCallbacks(
      SignalTools signalTools,
      StrategyTools strategyTools,
      RefinementTools refinementTools,
      RefinementDetailTools detailTools,
      SearchTools searchTools) {
    return MethodToolCallbackProvider.builder()
        .toolObjects(signalTools, strategyTools, refinementTools, detailTools, searchTools)
        .build();
  }
}
