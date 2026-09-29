package lab.wide.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.UUID;
import lab.test.TestBase;
import lab.wide.application.SignalLink;
import org.junit.jupiter.api.Test;

class SignalLinkInputTest extends TestBase {

  @Test
  void toApplication_keepsAbsentLinksAbsent() {
    assertThat(SignalLinkInput.toApplication(null)).isNull();
  }

  @Test
  void toApplication_convertsEachLinkAndPreservesNullEntriesForValidation() {
    var input = new SignalLinkInput(UUID.randomUUID(), randomString());

    var links = SignalLinkInput.toApplication(Arrays.asList(input, null));

    assertThat(links).containsExactly(new SignalLink(input.signalId(), input.rationale()), null);
  }
}
