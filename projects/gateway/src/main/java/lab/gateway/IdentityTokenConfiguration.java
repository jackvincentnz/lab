package lab.gateway;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdentityTokenConfiguration {
  @Bean
  SigningKey signingKey(IdentityTokenProperties properties) {
    return SigningKey.from(properties);
  }

  @Bean
  IdentityTokenMinter identityTokenMinter(IdentityTokenProperties properties, SigningKey key) {
    return new IdentityTokenMinter(properties, key, Clock.systemUTC());
  }
}
