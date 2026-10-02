package lab.wide.application;

import static lab.wide.application.FieldEdits.editFields;
import static lab.wide.application.FieldEdits.edited;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lab.wide.domain.Problem;
import lab.wide.domain.QuestionStatus;
import lab.wide.domain.RefinementDecision;
import lab.wide.domain.RefinementQuestion;
import lab.wide.domain.Solution;
import lab.wide.infrastructure.ProblemRepository;
import lab.wide.infrastructure.RefinementDecisionRepository;
import lab.wide.infrastructure.RefinementQuestionRepository;
import lab.wide.infrastructure.SolutionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefinementDetailService {
  private final RefinementQuestionRepository questions;
  private final RefinementDecisionRepository decisions;
  private final ProblemRepository problems;
  private final SolutionRepository solutions;

  public RefinementDetailService(
      RefinementQuestionRepository questions,
      RefinementDecisionRepository decisions,
      ProblemRepository problems,
      SolutionRepository solutions) {
    this.questions = questions;
    this.decisions = decisions;
    this.problems = problems;
    this.solutions = solutions;
  }

  public List<RefinementQuestion> questionsForProblem(UUID id) {
    return questions.findByProblemIdOrderByCreatedAtAscIdAsc(id);
  }

  public List<RefinementQuestion> questionsForSolution(UUID id) {
    return questions.findBySolutionIdOrderByCreatedAtAscIdAsc(id);
  }

  public List<RefinementDecision> decisionsForProblem(UUID id) {
    return decisions.findByProblemIdOrderByCreatedAtAscIdAsc(id);
  }

  public List<RefinementDecision> decisionsForSolution(UUID id) {
    return decisions.findBySolutionIdOrderByCreatedAtAscIdAsc(id);
  }

  private void parent(UUID problemId, UUID solutionId) {
    if ((problemId == null) == (solutionId == null))
      throw new IllegalArgumentException("Provide exactly one problemId or solutionId.");
    if (problemId != null && !problems.existsById(problemId))
      throw new IllegalArgumentException("Problem not found: " + problemId);
    if (solutionId != null && !solutions.existsById(solutionId))
      throw new IllegalArgumentException("Solution not found: " + solutionId);
  }

  private static void required(String value, String field) {
    if (value == null || value.isBlank())
      throw new IllegalArgumentException("Nonblank " + field + " is required.");
  }

  private static QuestionStatus status(String value) {
    try {
      return QuestionStatus.valueOf(value);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Question status must be OPEN or ANSWERED.");
    }
  }

  private static void validateQuestion(String question, QuestionStatus status, String answer) {
    required(question, "question");
    if (status == QuestionStatus.ANSWERED) required(answer, "answer for an answered question");
  }

  // Include detail changes in the parent's existing 'most recently updated' ordering.
  private void touch(UUID problemId, UUID solutionId, Instant now) {
    if (problemId != null) {
      var p = problems.findById(problemId).orElseThrow();
      problems.save(
          new Problem(
              p.id(),
              p.title(),
              p.description(),
              p.impact(),
              p.desiredOutcome(),
              p.createdAt(),
              now));
    } else {
      var s = solutions.findById(solutionId).orElseThrow();
      solutions.save(
          new Solution(
              s.id(),
              s.title(),
              s.approach(),
              s.scope(),
              s.tradeoffs(),
              s.effort(),
              s.createdAt(),
              now));
    }
  }

  @Transactional
  public RefinementQuestion createQuestion(
      UUID problemId,
      UUID solutionId,
      String question,
      QuestionStatus status,
      String answer,
      String context,
      String source) {
    parent(problemId, solutionId);
    var state = status == null ? QuestionStatus.OPEN : status;
    validateQuestion(question, state, answer);
    var now = Instant.now();
    var saved =
        questions.save(
            new RefinementQuestion(
                null, problemId, solutionId, question, state, answer, context, source, now, now));
    touch(problemId, solutionId, now);
    return saved;
  }

  @Transactional
  public RefinementQuestion editQuestion(
      UUID id,
      Map<String, String> fields,
      List<String> clearFields,
      UUID problemId,
      UUID solutionId) {
    boolean moving = problemId != null || solutionId != null;
    if (moving) parent(problemId, solutionId);
    var changes =
        moving
                && (fields == null || fields.isEmpty())
                && (clearFields == null || clearFields.isEmpty())
            ? Map.<String, String>of()
            : editFields(
                fields,
                clearFields,
                Set.of("question", "status", "answer", "context", "source"),
                Set.of("question", "status"));
    var q =
        questions
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Question not found: " + id));
    var text = edited(changes, "question", q.question());
    var state = status(edited(changes, "status", q.status().name()));
    var answer = edited(changes, "answer", q.answer());
    validateQuestion(text, state, answer);
    var now = Instant.now();
    var saved =
        questions.save(
            new RefinementQuestion(
                id,
                moving ? problemId : q.problemId(),
                moving ? solutionId : q.solutionId(),
                text,
                state,
                answer,
                edited(changes, "context", q.context()),
                edited(changes, "source", q.source()),
                q.createdAt(),
                now));
    touch(q.problemId(), q.solutionId(), now);
    if (moving
        && (!java.util.Objects.equals(q.problemId(), problemId)
            || !java.util.Objects.equals(q.solutionId(), solutionId)))
      touch(problemId, solutionId, now);
    return saved;
  }

  @Transactional
  public RefinementDecision createDecision(
      UUID problemId,
      UUID solutionId,
      String decision,
      String rationale,
      String source,
      String revisitWhen) {
    parent(problemId, solutionId);
    required(decision, "decision");
    var now = Instant.now();
    var saved =
        decisions.save(
            new RefinementDecision(
                null, problemId, solutionId, decision, rationale, source, revisitWhen, now, now));
    touch(problemId, solutionId, now);
    return saved;
  }

  @Transactional
  public RefinementDecision editDecision(
      UUID id, Map<String, String> fields, List<String> clearFields) {
    var changes =
        editFields(
            fields,
            clearFields,
            Set.of("decision", "rationale", "source", "revisitWhen"),
            Set.of("decision"));
    var d =
        decisions
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Decision not found: " + id));
    var text = edited(changes, "decision", d.decision());
    required(text, "decision");
    var now = Instant.now();
    var saved =
        decisions.save(
            new RefinementDecision(
                id,
                d.problemId(),
                d.solutionId(),
                text,
                edited(changes, "rationale", d.rationale()),
                edited(changes, "source", d.source()),
                edited(changes, "revisitWhen", d.revisitWhen()),
                d.createdAt(),
                now));
    touch(d.problemId(), d.solutionId(), now);
    return saved;
  }
}
