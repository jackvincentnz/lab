package lab.wide.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.UUID;
import lab.test.TestBase;
import lab.wide.application.ProblemLink;
import org.junit.jupiter.api.Test;

class ProblemLinkInputTest extends TestBase {

  @Test
  void toApplication_keepsAbsentLinksAbsent() {
    assertThat(ProblemLinkInput.toApplication(null)).isNull();
  }

  @Test
  void toApplication_convertsEachLinkAndPreservesNullEntriesForValidation() {
    var input = new ProblemLinkInput(UUID.randomUUID(), randomString());

    var links = ProblemLinkInput.toApplication(Arrays.asList(input, null));

    assertThat(links).containsExactly(new ProblemLink(input.problemId(), input.rationale()), null);
  }
}
