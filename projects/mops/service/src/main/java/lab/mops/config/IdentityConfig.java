package lab.mops.config;

import lab.libs.identity.web.IdentityFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class IdentityConfig {

  @Bean
  FilterRegistrationBean<IdentityFilter> identityFilter(IdentityProperties properties) {
    var filter =
        new IdentityFilter(
            properties.publicPaths(), properties.development().identity().orElse(null));
    var registration = new FilterRegistrationBean<>(filter);
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return registration;
  }
}
