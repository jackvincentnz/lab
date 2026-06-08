package lab.mops.config;

import lab.spring.http.LoggingHttpRequestInterceptor;
import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientConfig {

  @Bean
  public RestClientCustomizer loggingRestClientCustomizer() {
    return builder -> builder.requestInterceptor(new LoggingHttpRequestInterceptor());
  }
}
