package lab.wide.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.UUID;
import lab.test.TestBase;
import lab.wide.application.ProblemLink;
import org.junit.jupiter.api.Test;

class ProblemLinkInputTest extends TestBase {

  @Test
  void toApplication_noLinks_returnsNull() {
    var problemLinks = ProblemLinkInput.toApplication(null);

    assertThat(problemLinks).isNull();
  }

  @Test
  void toApplication_linkAndNullEntry_convertsTheLinkAndKeepsTheNullForValidation() {
    var problemLinkInput = new ProblemLinkInput(UUID.randomUUID(), randomString());

    var problemLinks = ProblemLinkInput.toApplication(Arrays.asList(problemLinkInput, null));

    assertThat(problemLinks)
        .containsExactly(
            new ProblemLink(problemLinkInput.problemId(), problemLinkInput.rationale()), null);
  }
}
