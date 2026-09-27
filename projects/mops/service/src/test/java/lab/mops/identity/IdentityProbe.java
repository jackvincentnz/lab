package lab.mops.identity;

import lab.libs.identity.Identity;
import lab.libs.identity.IdentityHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Echoes the identity a request was dispatched as, so tests can see what the filters resolved. */
@RestController
class IdentityProbe {

  @GetMapping("/identity-probe")
  Identity identity() {
    return IdentityHolder.get().orElseThrow();
  }
}
