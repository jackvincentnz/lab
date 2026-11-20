package lab.wide.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.UUID;
import lab.test.TestBase;
import lab.wide.application.SignalLink;
import org.junit.jupiter.api.Test;

class SignalLinkInputTest extends TestBase {

  @Test
  void toApplication_noLinks_returnsNull() {
    var signalLinks = SignalLinkInput.toApplication(null);

    assertThat(signalLinks).isNull();
  }

  @Test
  void toApplication_linkAndNullEntry_convertsTheLinkAndKeepsTheNullForValidation() {
    var signalLinkInput = new SignalLinkInput(UUID.randomUUID(), randomString());

    var signalLinks = SignalLinkInput.toApplication(Arrays.asList(signalLinkInput, null));

    assertThat(signalLinks)
        .containsExactly(
            new SignalLink(signalLinkInput.signalId(), signalLinkInput.rationale()), null);
  }
}
