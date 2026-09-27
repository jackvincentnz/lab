package lab.gateway;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import org.springframework.security.web.server.authentication.logout.WebSessionServerLogoutHandler;
import reactor.core.publisher.Mono;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(GatewayUsers.class)
public class SecurityConfiguration {
  @Bean
  ReactiveUserDetailsService users(GatewayUsers configuration) {
    Map<String, GatewayUsers.ConfiguredUser> users =
        configuration.users().stream()
            .collect(
                Collectors.toUnmodifiableMap(
                    GatewayUsers.ConfiguredUser::username, Function.identity()));
    // Authentication erases credentials. Return a fresh principal for every login attempt.
    return username -> Mono.justOrEmpty(users.get(username)).map(GatewayPrincipal::new);
  }

  @Bean
  ReactiveAuthenticationManager authenticationManager(ReactiveUserDetailsService users) {
    var manager = new UserDetailsRepositoryReactiveAuthenticationManager(users);
    return authentication ->
        manager
            .authenticate(authentication)
            .doOnNext(result -> ((CredentialsContainer) result).eraseCredentials());
  }

  @Bean
  SecurityWebFilterChain securityWebFilterChain(
      ServerHttpSecurity http, ReactiveAuthenticationManager authenticationManager) {
    var invalidateSession = new WebSessionServerLogoutHandler();
    var redirect = new RedirectServerLogoutSuccessHandler();
    return http.authenticationManager(authenticationManager)
        .authorizeExchange(
            exchanges ->
                exchanges
                    .pathMatchers("/actuator/health")
                    .permitAll()
                    .anyExchange()
                    .authenticated())
        .formLogin(withDefaults())
        .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
        .logout(
            logout ->
                // Invalidate after the default security-context and CSRF logout handlers finish.
                logout.logoutSuccessHandler(
                    (exchange, authentication) ->
                        invalidateSession
                            .logout(exchange, authentication)
                            .then(
                                Mono.defer(
                                    () -> redirect.onLogoutSuccess(exchange, authentication)))))
        .build();
  }
}
