package lab.mops;

import static org.assertj.core.api.Assertions.assertThat;

import lab.test.TestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

class MopsApplicationResetTest extends TestBase {

  private final MockMvcTester mockMvcTester = MockMvcTester.of(new MopsApplication());

  @Test
  void reset_withGet_isNotAllowed() {
    var response = mockMvcTester.get().uri("/reset");

    assertThat(response).hasStatus(HttpStatus.METHOD_NOT_ALLOWED);
  }
}
