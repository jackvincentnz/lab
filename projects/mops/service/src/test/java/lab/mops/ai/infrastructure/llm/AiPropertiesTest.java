package lab.mops.ai.infrastructure.llm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import lab.test.TestBase;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;

class AiPropertiesTest extends TestBase {

  @Test
  void systemPromptText_defaultLocation_returnsPackagedPrompt() {
    var resource = new DefaultResourceLoader().getResource("classpath:prompts/system.txt");

    var text = new AiProperties(resource).systemPromptText();

    assertThat(text)
        .startsWith("You are a skilled expert in planning and budgeting for marketing campaigns.\n")
        .contains("type QuarterlyTotal {\n  quarter: Quarter!\n  fiscalYear: Int!\n")
        .contains("4. **Call the tools.**")
        .endsWith("6. **Ask the user if they need anything else.**\n");
  }

  @Test
  void constructor_missingResource_failsFast() {
    var missing = new ClassPathResource(randomString());

    assertThatThrownBy(() -> new AiProperties(missing))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageStartingWith("mops.ai.system-prompt resource not found: ");
  }

  @Test
  void constructor_unsetResource_failsFast() {
    assertThatThrownBy(() -> new AiProperties(null))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("mops.ai.system-prompt is not set");
  }
}
