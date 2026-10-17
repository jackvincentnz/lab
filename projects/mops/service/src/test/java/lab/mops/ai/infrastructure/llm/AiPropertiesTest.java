package lab.mops.ai.infrastructure.llm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import lab.test.TestBase;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;

class AiPropertiesTest extends TestBase {

  @Test
  void systemPromptText_defaultLocation_returnsText() {
    var resource = new DefaultResourceLoader().getResource("classpath:prompts/system.txt");

    var text = new AiProperties(resource).systemPromptText();

    assertThat(text).isNotBlank();
  }

  @Test
  void constructor_unsetResource_failsFast() {
    assertThatThrownBy(() -> new AiProperties(null))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("mops.ai.system-prompt is not set");
  }

  @Test
  void constructor_missingResource_failsFast() {
    var missing = new ClassPathResource(randomString());

    assertThatThrownBy(() -> new AiProperties(missing))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageStartingWith("mops.ai.system-prompt resource not found: ");
  }
}
