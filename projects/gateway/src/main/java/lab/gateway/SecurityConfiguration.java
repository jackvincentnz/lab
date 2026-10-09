package lab.gateway;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.web.server.DelegatingServerAuthenticationEntryPoint;
import org.springframework.security.web.server.DelegatingServerAuthenticationEntryPoint.DelegateEntry;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.security.web.server.csrf.WebSessionServerCsrfTokenRepository;
import org.springframework.security.web.server.ui.DefaultResourcesWebFilter;
import org.springframework.security.web.server.ui.LoginPageGeneratingWebFilter;
import org.springframework.security.web.server.ui.LogoutPageGeneratingWebFilter;
import org.springframework.security.web.server.util.matcher.MediaTypeServerWebExchangeMatcher;
import reactor.core.publisher.Mono;

@Configuration
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

  /** Shared with the access log, so it reads the caller from where the security chain does. */
  @Bean
  ServerSecurityContextRepository securityContextRepository() {
    return new WebSessionServerSecurityContextRepository();
  }

  @Bean
  SecurityWebFilterChain securityWebFilterChain(
      ServerHttpSecurity http,
      ReactiveAuthenticationManager authenticationManager,
      ServerSecurityContextRepository securityContexts) {
    var csrfTokens = new WebSessionServerCsrfTokenRepository();
    var loginPage = new LoginPageGeneratingWebFilter();
    loginPage.setFormLoginEnabled(true);
    return http.authenticationManager(authenticationManager)
        .securityContextRepository(securityContexts)
        .authorizeExchange(
            exchanges ->
                exchanges
                    .pathMatchers("/actuator/health", JwkSetController.PATH)
                    .permitAll()
                    .anyExchange()
                    .authenticated())
        .formLogin(withDefaults())
        // A bearer header is never sent by the browser on its own, so the resource server exempts
        // bearer requests from CSRF.
        .oauth2ResourceServer(server -> server.jwt(withDefaults()))
        .csrf(csrf -> csrf.csrfTokenRepository(csrfTokens))
        .logout(ServerHttpSecurity.LogoutSpec::disable)
        .addFilterAt(GatewayLogout.filter(csrfTokens), SecurityWebFiltersOrder.LOGOUT)
        .exceptionHandling(handling -> handling.authenticationEntryPoint(entryPoint()))
        // Spring omits its generated pages once the entry point is explicit.
        .addFilterAt(loginPage, SecurityWebFiltersOrder.LOGIN_PAGE_GENERATING)
        .addFilterBefore(
            DefaultResourcesWebFilter.css(), SecurityWebFiltersOrder.LOGIN_PAGE_GENERATING)
        .addFilterAt(
            new LogoutPageGeneratingWebFilter(), SecurityWebFiltersOrder.LOGOUT_PAGE_GENERATING)
        .build();
  }

  /** Browsers are sent to the login form; API clients get a status they can act on. */
  private static ServerAuthenticationEntryPoint entryPoint() {
    var html = new MediaTypeServerWebExchangeMatcher(MediaType.TEXT_HTML);
    html.setIgnoredMediaTypes(Set.of(MediaType.ALL));
    var entryPoint =
        new DelegatingServerAuthenticationEntryPoint(
            new DelegateEntry(html, new RedirectServerAuthenticationEntryPoint("/login")));
    entryPoint.setDefaultEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED));
    return entryPoint;
  }
}
