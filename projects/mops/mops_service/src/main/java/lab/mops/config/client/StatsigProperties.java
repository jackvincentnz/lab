package lab.mops.config.client;

import java.util.Optional;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("statsig")
public final class StatsigProperties {

  private final String clientKey;

  public StatsigProperties(String clientKey) {
    this.clientKey = clientKey;
  }

  public Optional<String> getClientKey() {
    return Optional.ofNullable(clientKey);
  }
}
