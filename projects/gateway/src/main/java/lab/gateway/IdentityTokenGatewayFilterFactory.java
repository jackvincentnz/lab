package lab.gateway;

import java.util.List;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

/**
 * Route filter {@code IdentityToken=<audience>}: forwards the session's identity to the vertical as
 * a bearer token. A request the gateway did not authenticate is refused rather than forwarded,
 * since a vertical running without an issuer may treat a tokenless request as its development
 * identity.
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
            .flatMap(authentication -> Mono.justOrEmpty(Caller.of(authentication)))
            .switchIfEmpty(
                Mono.error(
                    () ->
                        new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED, "The route needs a gateway session")))
            .zipWith(exchange.getSession().map(WebSession::getId))
            .map(caller -> minter.mint(caller.getT1(), caller.getT2(), config.getAudience()))
            .flatMap(
                token ->
                    chain.filter(
                        exchange
                            .mutate()
                            .request(
                                request -> request.headers(headers -> headers.setBearerAuth(token)))
                            .build()));
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
