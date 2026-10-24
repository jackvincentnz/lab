package lab.gateway;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

/**
 * Route filter {@code IdentityToken=<audience>}: forwards the caller's identity to the downstream
 * service as a bearer token. A request the gateway did not authenticate is refused rather than
 * forwarded, since a service running without an issuer may treat a tokenless request as its
 * development identity.
 */
@Component
public class IdentityTokenGatewayFilterFactory
    extends AbstractGatewayFilterFactory<IdentityTokenGatewayFilterFactory.Config> {

  private final IdentityTokenMinter minter;

  public IdentityTokenGatewayFilterFactory(IdentityTokenMinter minter) {
    super(Config.class);
    this.minter = minter;
  }

  @Override
  public List<String> shortcutFieldOrder() {
    return List.of("audience");
  }

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) ->
        exchange
            .getPrincipal()
            .map(Optional::of)
            .defaultIfEmpty(Optional.empty())
            .flatMap(
                principal ->
                    Mono.justOrEmpty(
                            principal
                                .filter(Authentication.class::isInstance)
                                .map(Authentication.class::cast)
                                .flatMap(Caller::of))
                        .switchIfEmpty(
                            Mono.error(() -> withoutCaller(config.getAudience(), principal))))
            .map(caller -> minter.mint(caller, config.getAudience()))
            .flatMap(
                token ->
                    chain.filter(
                        exchange
                            .mutate()
                            .request(
                                request -> request.headers(headers -> headers.setBearerAuth(token)))
                            .build()));
  }

  /** Security authenticates every request on an identity route, so this is a misconfiguration. */
  private static IllegalStateException withoutCaller(
      String audience, Optional<? extends Principal> principal) {
    var authentication = principal.map(found -> found.getClass().getSimpleName()).orElse("none");
    return new IllegalStateException(
        "Route to "
            + audience
            + " reached without a caller: authentication was "
            + authentication
            + ". Security must authenticate every request on this route with a session or bearer"
            + " token.");
  }

  /** The downstream service a route forwards to, which every token it carries is addressed to. */
  public static class Config {
    private String audience;

    public String getAudience() {
      return audience;
    }

    public void setAudience(String audience) {
      Assert.hasText(audience, "IdentityToken needs the audience of the service it forwards to");
      this.audience = audience;
    }
  }
}
