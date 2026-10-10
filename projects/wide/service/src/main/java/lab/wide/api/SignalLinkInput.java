package lab.wide.api;

import java.util.List;
import java.util.UUID;
import lab.wide.application.SignalLink;
import org.springframework.ai.tool.annotation.ToolParam;

public record SignalLinkInput(
    @ToolParam(description = "Existing signal UUID") UUID signalId,
    @ToolParam(description = "Source role, attribution, and uncertainty") String rationale) {
  static List<SignalLink> toApplication(List<SignalLinkInput> links) {
    if (links == null) return null;
    return links.stream()
        .map(link -> link == null ? null : new SignalLink(link.signalId(), link.rationale()))
        .toList();
  }
}
