package lab.gateway;

import java.util.List;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.server.WebSession;

/**
 * Route filter {@code IdentityToken=<audience>}: forwards the session's identity to the vertical as
 * a bearer token. A request the gateway did not authenticate is forwarded without one.
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
            .<Authentication>getPrincipal()
            .map(Authentication::getPrincipal)
            .ofType(GatewayPrincipal.class)
            .zipWith(exchange.getSession().map(WebSession::getId))
            .map(identity -> minter.mint(identity.getT1(), identity.getT2(), config.getAudience()))
            .map(
                token ->
                    exchange
                        .mutate()
                        .request(
                            request -> request.headers(headers -> headers.setBearerAuth(token)))
                        .build())
            .defaultIfEmpty(exchange)
            .flatMap(chain::filter);
  }

  /** The vertical a route forwards to, which every token it carries is addressed to. */
  public static class Config {
    private String audience;

    public String getAudience() {
      return audience;
    }

    public void setAudience(String audience) {
      Assert.hasText(audience, "IdentityToken needs the audience of the vertical it forwards to");
      this.audience = audience;
    }
  }
}
