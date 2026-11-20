package lab.wide.application;

import static lab.wide.application.FieldEdits.editFields;
import static lab.wide.application.FieldEdits.edited;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lab.wide.domain.ProblemRepository;
import lab.wide.domain.QuestionStatus;
import lab.wide.domain.RefinementDecision;
import lab.wide.domain.RefinementDecisionRepository;
import lab.wide.domain.RefinementQuestion;
import lab.wide.domain.RefinementQuestionRepository;
import lab.wide.domain.SolutionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefinementDetailService {
  private final RefinementQuestionRepository refinementQuestionRepository;
  private final RefinementDecisionRepository refinementDecisionRepository;
  private final ProblemRepository problemRepository;
  private final SolutionRepository solutionRepository;
  private final Clock clock;

  public RefinementDetailService(
      RefinementQuestionRepository refinementQuestionRepository,
      RefinementDecisionRepository refinementDecisionRepository,
      ProblemRepository problemRepository,
      SolutionRepository solutionRepository,
      Clock clock) {
    this.refinementQuestionRepository = refinementQuestionRepository;
    this.refinementDecisionRepository = refinementDecisionRepository;
    this.problemRepository = problemRepository;
    this.solutionRepository = solutionRepository;
    this.clock = clock;
  }

  public List<RefinementQuestion> questionsForProblem(UUID id) {
    return refinementQuestionRepository.findByProblemIdOrderByCreatedAtAscIdAsc(id);
  }

  public List<RefinementQuestion> questionsForSolution(UUID id) {
    return refinementQuestionRepository.findBySolutionIdOrderByCreatedAtAscIdAsc(id);
  }

  public List<RefinementDecision> decisionsForProblem(UUID id) {
    return refinementDecisionRepository.findByProblemIdOrderByCreatedAtAscIdAsc(id);
  }

  public List<RefinementDecision> decisionsForSolution(UUID id) {
    return refinementDecisionRepository.findBySolutionIdOrderByCreatedAtAscIdAsc(id);
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
    var questionStatus = status == null ? QuestionStatus.OPEN : status;
    validateQuestion(question, questionStatus, answer);

    var now = Instant.now(clock);
    var refinementQuestion =
        refinementQuestionRepository.save(
            new RefinementQuestion(
                null,
                problemId,
                solutionId,
                question,
                questionStatus,
                answer,
                context,
                source,
                now,
                now));

    touch(problemId, solutionId, now);

    return refinementQuestion;
  }

  private void parent(UUID problemId, UUID solutionId) {
    if ((problemId == null) == (solutionId == null)) {
      throw new IllegalArgumentException("Provide exactly one problemId or solutionId.");
    }
    if (problemId != null && !problemRepository.existsById(problemId)) {
      throw new IllegalArgumentException("Problem not found: " + problemId);
    }
    if (solutionId != null && !solutionRepository.existsById(solutionId)) {
      throw new IllegalArgumentException("Solution not found: " + solutionId);
    }
  }

  private static void validateQuestion(String question, QuestionStatus status, String answer) {
    required(question, "question");
    if (status == QuestionStatus.ANSWERED) {
      required(answer, "answer for an answered question");
    }
  }

  private static void required(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Nonblank " + field + " is required.");
    }
  }

  // Include detail changes in the parent's existing 'most recently updated' ordering.
  private void touch(UUID problemId, UUID solutionId, Instant now) {
    if (problemId != null) {
      problemRepository.touch(problemId, now);
    } else {
      solutionRepository.touch(solutionId, now);
    }
  }

  @Transactional
  public RefinementQuestion editQuestion(
      UUID id,
      Map<String, String> fields,
      List<String> clearFields,
      UUID problemId,
      UUID solutionId) {
    boolean moving = problemId != null || solutionId != null;
    if (moving) {
      parent(problemId, solutionId);
    }
    var changes =
        moving && isEmpty(fields) && isEmpty(clearFields)
            ? Map.<String, String>of()
            : editFields(
                fields,
                clearFields,
                Set.of("question", "status", "answer", "context", "source"),
                Set.of("question", "status"));
    var refinementQuestion =
        refinementQuestionRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Question not found: " + id));

    var question = edited(changes, "question", refinementQuestion.question());
    var status = status(edited(changes, "status", refinementQuestion.status().name()));
    var answer = edited(changes, "answer", refinementQuestion.answer());
    validateQuestion(question, status, answer);

    var now = Instant.now(clock);
    var editedQuestion =
        refinementQuestionRepository.save(
            new RefinementQuestion(
                id,
                moving ? problemId : refinementQuestion.problemId(),
                moving ? solutionId : refinementQuestion.solutionId(),
                question,
                status,
                answer,
                edited(changes, "context", refinementQuestion.context()),
                edited(changes, "source", refinementQuestion.source()),
                refinementQuestion.createdAt(),
                now));

    touch(refinementQuestion.problemId(), refinementQuestion.solutionId(), now);
    if (moving
        && (!Objects.equals(refinementQuestion.problemId(), problemId)
            || !Objects.equals(refinementQuestion.solutionId(), solutionId))) {
      touch(problemId, solutionId, now);
    }

    return editedQuestion;
  }

  private static boolean isEmpty(Map<String, String> fields) {
    return fields == null || fields.isEmpty();
  }

  private static boolean isEmpty(List<String> clearFields) {
    return clearFields == null || clearFields.isEmpty();
  }

  private static QuestionStatus status(String value) {
    try {
      return QuestionStatus.valueOf(value);
    } catch (IllegalArgumentException illegalArgumentException) {
      throw new IllegalArgumentException("Question status must be OPEN or ANSWERED.");
    }
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

    var now = Instant.now(clock);
    var refinementDecision =
        refinementDecisionRepository.save(
            new RefinementDecision(
                null, problemId, solutionId, decision, rationale, source, revisitWhen, now, now));

    touch(problemId, solutionId, now);

    return refinementDecision;
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
    var refinementDecision =
        refinementDecisionRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Decision not found: " + id));

    var decision = edited(changes, "decision", refinementDecision.decision());
    required(decision, "decision");

    var now = Instant.now(clock);
    var editedDecision =
        refinementDecisionRepository.save(
            new RefinementDecision(
                id,
                refinementDecision.problemId(),
                refinementDecision.solutionId(),
                decision,
                edited(changes, "rationale", refinementDecision.rationale()),
                edited(changes, "source", refinementDecision.source()),
                edited(changes, "revisitWhen", refinementDecision.revisitWhen()),
                refinementDecision.createdAt(),
                now));

    touch(refinementDecision.problemId(), refinementDecision.solutionId(), now);

    return editedDecision;
  }
}
