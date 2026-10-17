package lab.wide.api;

import java.util.UUID;
import lab.wide.application.SignalService;
import lab.wide.domain.Signal;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class SignalTools {

  private final SignalService service;

  public SignalTools(SignalService service) {
    this.service = service;
  }

  @Tool(
      name = "wide_capture_signal",
      description =
          """
          Capture a distinct observation, idea, or concern worth considering, including discoveries
          during refinement. Preserve supplied wording, attribution, and uncertainty. Clarifications,
          answers, decisions, and agent reasoning about known work belong in its problem or candidate;
          they do not automatically become signals. Source is optional; never invent one.
          WIDE generates the signal ID. Each call creates a new signal and returns its ID.
          """)
  public WriteReceipt capture(
      @ToolParam(description = "Concise descriptive headline written by the agent") String title,
      @ToolParam(description = "Observation or idea as supplied; preserve wording and uncertainty")
          String content,
      @ToolParam(
              required = false,
              description =
                  "Where the information came from: person, context, or reference; omit if unknown")
          String source) {
    return new WriteReceipt(service.capture(title, content, source).id());
  }

  @Tool(name = "wide_get_signal", description = "Retrieve one saved WIDE signal by UUID.")
  public Signal get(@ToolParam(description = "Signal UUID") UUID id) {
    return service.get(id);
  }
}
