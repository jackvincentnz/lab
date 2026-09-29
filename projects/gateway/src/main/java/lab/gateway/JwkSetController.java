package lab.gateway;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public final class JwkSetController {
  private final DownstreamIdentity identity;

  public JwkSetController(DownstreamIdentity identity) {
    this.identity = identity;
  }

  @GetMapping("/.well-known/jwks.json")
  public Map<String, Object> keys() {
    return identity.publicKeys();
  }
}
