package lab.wide.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import lab.wide.application.RefinementDetailService;
import lab.wide.domain.QuestionStatus;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class RefinementDetailTools {
  private final RefinementDetailService service;

  public RefinementDetailTools(RefinementDetailService service) {
    this.service = service;
  }

  @Tool(
      name = "wide_create_question",
      description =
          "Save one refinement question on exactly one existing problem or candidate solution. Read"
              + " the parent's get tool first to avoid duplicates. WIDE generates the UUID. Returns"
              + " the saved ID. Questions and answers need no new signal. Use during user-directed"
              + " refinement, not read-only triage.")
  public WriteReceipt createQuestion(
      @ToolParam(
              required = false,
              description = "Existing problem UUID; provide this or solutionId, not both")
          UUID problemId,
      @ToolParam(
              required = false,
              description = "Existing solution UUID; provide this or problemId, not both")
          UUID solutionId,
      @ToolParam(description = "One question that could improve a decision") String question,
      @ToolParam(
              required = false,
              description = "OPEN (default) or ANSWERED; ANSWERED requires a nonblank answer")
          QuestionStatus status,
      @ToolParam(
              required = false,
              description = "What was learned, with uncertainty; do not invent an answer")
          String answer,
      @ToolParam(
              required = false,
              description = "Why the question matters, supporting context, or limitations")
          String context,
      @ToolParam(
              required = false,
              description =
                  "Attribution for supporting information or the answer: person, conversation,"
                      + " reference, or existing signal ID; no new signal required")
          String source) {
    return new WriteReceipt(
        service
            .createQuestion(problemId, solutionId, question, status, answer, context, source)
            .id());
  }

  @Tool(
      name = "wide_edit_question",
      description =
          "Edit an individual question returned by wide_get_problem or wide_get_solution. Read"
              + " first; omitted fields stay unchanged. Use clearFields to clear optional fields."
              + " Set status explicitly when answering or reopening; reopening preserves any"
              + " earlier answer unless explicitly cleared. ANSWERED requires a nonblank answer."
              + " Moves preserve question identity, status, answer and source; both parents are"
              + " updated. No other question, decision, or signal changes. Returns the saved ID;"
              + " parent update time advances.")
  public WriteReceipt editQuestion(
      @ToolParam(description = "Existing question UUID") UUID id,
      @ToolParam(
              required = false,
              description =
                  "Text fields to change: question, status (OPEN or ANSWERED), answer, context,"
                      + " source")
          Map<String, String> fields,
      @ToolParam(
              required = false,
              description =
                  "Optional fields to clear: answer, context, source; cannot also be set in fields")
          List<String> clearFields,
      @ToolParam(
              required = false,
              description = "Move to this problem; omit both parent IDs to keep the current parent")
          UUID problemId,
      @ToolParam(
              required = false,
              description = "Move to this candidate; provide at most one parent ID")
          UUID solutionId) {
    return new WriteReceipt(
        service.editQuestion(id, fields, clearFields, problemId, solutionId).id());
  }

  @Tool(
      name = "wide_create_decision",
      description =
          "Record an explicitly stated human position on exactly one problem or candidate. Read its"
              + " get tool first to avoid duplicates. Preserve attribution and conditions; agent"
              + " suggestions belong in draft reasoning, not recorded human decisions. A decision"
              + " is not delivery authorization. WIDE generates the UUID; returns the saved ID. No"
              + " new signal required. Use during user-directed refinement.")
  public WriteReceipt createDecision(
      @ToolParam(
              required = false,
              description = "Existing problem UUID; provide this or solutionId, not both")
          UUID problemId,
      @ToolParam(
              required = false,
              description = "Existing solution UUID; provide this or problemId, not both")
          UUID solutionId,
      @ToolParam(description = "The human's stated position, preserving qualifications")
          String decision,
      @ToolParam(
              required = false,
              description =
                  "Reasoning or context provided for the decision; distinguish interpretation")
          String rationale,
      @ToolParam(
              required = false,
              description =
                  "Who stated the decision and where; use known attribution, conversation or"
                      + " reference, without inventing details")
          String source,
      @ToolParam(
              required = false,
              description = "Stated condition for reconsidering the decision; omit if none given")
          String revisitWhen) {
    return new WriteReceipt(
        service
            .createDecision(problemId, solutionId, decision, rationale, source, revisitWhen)
            .id());
  }

  @Tool(
      name = "wide_edit_decision",
      description =
          "Update a recorded human decision from the parent's get tool, preserving omitted fields."
              + " Use for a correction or a changed human position, never to substitute an agent"
              + " preference. Use clearFields for optional fields. Parent, ID, and creation time"
              + " remain; parent update time advances. Returns the saved ID. No revision"
              + " history is retained.")
  public WriteReceipt editDecision(
      @ToolParam(description = "Existing decision UUID") UUID id,
      @ToolParam(
              required = false,
              description = "Text fields to change: decision, rationale, source, revisitWhen")
          Map<String, String> fields,
      @ToolParam(
              required = false,
              description =
                  "Optional fields to clear: rationale, source, revisitWhen; cannot also be set in"
                      + " fields")
          List<String> clearFields) {
    return new WriteReceipt(service.editDecision(id, fields, clearFields).id());
  }
}
