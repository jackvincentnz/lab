package lab.mops.ai.infrastructure.llm;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

/**
 * How Mops instructs the chat model.
 *
 * @param systemPrompt where the system prompt is read from, so an operator can replace it without a
 *     rebuild. Checked when the properties bind so a wrong location stops startup instead of
 *     failing the first chat request.
 */
@ConfigurationProperties("mops.ai")
record AiProperties(Resource systemPrompt) {

  AiProperties {
    if (systemPrompt == null) {
      throw new IllegalStateException("mops.ai.system-prompt is not set");
    }
    if (!systemPrompt.exists()) {
      throw new IllegalStateException(
          "mops.ai.system-prompt resource not found: " + systemPrompt.getDescription());
    }
  }

  String systemPromptText() {
    try {
      return systemPrompt.getContentAsString(StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException(
          "Failed to read mops.ai.system-prompt: " + systemPrompt.getDescription(), e);
    }
  }
}
