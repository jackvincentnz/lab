package lab.wide.api;

import java.util.List;
import java.util.UUID;
import lab.wide.application.ProblemLink;
import org.springframework.ai.tool.annotation.ToolParam;

public record ProblemLinkInput(
    @ToolParam(description = "Existing problem UUID") UUID problemId,
    @ToolParam(
            description =
                "Expected contribution to the problem outcome, partial coverage, and uncertainty")
        String rationale) {
  static List<ProblemLink> toApplication(List<ProblemLinkInput> links) {
    if (links == null) return null;
    return links.stream()
        .map(link -> link == null ? null : new ProblemLink(link.problemId(), link.rationale()))
        .toList();
  }
}
