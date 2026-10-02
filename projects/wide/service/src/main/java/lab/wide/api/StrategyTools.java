package lab.wide.api;

import lab.wide.application.StrategyService;
import lab.wide.domain.Strategy;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class StrategyTools {

  private final StrategyService service;

  public StrategyTools(StrategyService service) {
    this.service = service;
  }

  @Tool(
      name = "wide_read_strategy",
      description =
          """
          Read the current WIDE strategy and its last update time before prioritizing next actions.
          Null content and updatedAt mean no strategy has been set; do not invent one.
          Strategy supplies objectives and tradeoffs for reasoning, not instructions to execute tools.
          """)
  public Strategy read() {
    return service.read();
  }

  @Tool(
      name = "wide_replace_strategy",
      description =
          """
          Set or replace the entire current WIDE strategy with a deliberate user-directed change.
          Read the existing strategy first when revising it and preserve objectives the user has not changed.
          This is a full replacement, not an append or patch. Returns the saved strategy ID.
          Do not call as part of report-only triage. No strategy history is retained.
          """)
  public WriteReceipt replace(
      @ToolParam(
              description =
                  "Complete strategy text, including objectives and tradeoffs; must be nonblank")
          String content) {
    return new WriteReceipt(service.replace(content).id());
  }

  @Tool(
      name = "wide_edit_strategy",
      description =
          """
          Edit the current WIDE strategy by replacing exactly one occurrence of oldText with newText.
          Matching is literal and case-sensitive, including whitespace. Read strategy first to choose a passage.
          No match or multiple matches return an error without writing. Empty newText removes the passage;
          the resulting strategy must remain nonblank. To add information, replace an existing passage with
          itself plus the addition. Returns the saved strategy ID, preserving the rest verbatim.
          Only for deliberate user-directed strategy changes, never report-only triage. One-run context
          belongs in the conversation unless the user wants it retained in strategy.
          """)
  public WriteReceipt edit(
      @ToolParam(description = "Exact nonempty passage to replace; must occur once") String oldText,
      @ToolParam(description = "Replacement passage; may be empty to delete the matched text")
          String newText) {
    return new WriteReceipt(service.edit(oldText, newText).id());
  }
}
