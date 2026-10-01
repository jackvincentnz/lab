package lab.gateway;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Publishes the gateway's token signing keys so verticals can verify what it forwards them. */
@RestController
public class JwkSetController {

  static final String PATH = "/.well-known/jwks.json";

  private final SigningKey key;

  public JwkSetController(SigningKey key) {
    this.key = key;
  }

  @GetMapping(PATH)
  Map<String, Object> jwkSet() {
    return key.publicJwkSet();
  }
}
