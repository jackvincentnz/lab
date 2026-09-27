package lab.mops.config;

import lab.libs.identity.jwt.IdentityJwtConverter;
import lab.libs.identity.jwt.IdentityJwtDecoder;
import lab.libs.identity.web.DevelopmentIdentityFilter;
import lab.libs.identity.web.IdentityHolderFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

/**
 * Mops is a resource server for identity tokens from one trusted issuer. Health is public; every
 * other request must carry a valid token. CSRF and sessions are the issuer's concern, since the
 * only credential Mops ever sees is a bearer token.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  JwtDecoder jwtDecoder(IdentityProperties properties) {
    return IdentityJwtDecoder.create(
        properties.jwkSetUri(), properties.issuer(), properties.audience());
  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http, IdentityProperties properties)
      throws Exception {
    http.csrf(CsrfConfigurer::disable)
        .sessionManagement(
            sessions -> sessions.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            requests ->
                requests
                    // The container reports a failed request by dispatching to /error without
                    // re-running bearer authentication; securing it would turn every server
                    // error into a 401.
                    .requestMatchers("/actuator/health", "/actuator/health/**", "/error")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(
            server -> server.jwt(jwt -> jwt.jwtAuthenticationConverter(new IdentityJwtConverter())))
        .addFilterAfter(new IdentityHolderFilter(), AuthorizationFilter.class);

    properties
        .development()
        .identity()
        .ifPresent(
            identity ->
                http.addFilterBefore(
                    new DevelopmentIdentityFilter(identity),
                    BearerTokenAuthenticationFilter.class));

    return http.build();
  }
}
