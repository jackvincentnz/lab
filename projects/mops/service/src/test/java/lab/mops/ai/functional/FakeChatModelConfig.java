package lab.mops.ai.functional;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class FakeChatModelConfig {

  // Primary because the provider auto-configuration still registers its own ChatModel.
  @Bean
  @Primary
  public FakeChatModel fakeChatModel() {
    return new FakeChatModel();
  }
}
