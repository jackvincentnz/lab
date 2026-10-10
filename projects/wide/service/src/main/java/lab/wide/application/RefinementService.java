package lab.wide.application;

import static lab.wide.application.FieldEdits.editFields;
import static lab.wide.application.FieldEdits.edited;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lab.wide.domain.Problem;
import lab.wide.domain.ProblemSignal;
import lab.wide.domain.Solution;
import lab.wide.domain.SolutionProblem;
import lab.wide.domain.SolutionSignal;
import lab.wide.infrastructure.ProblemRepository;
import lab.wide.infrastructure.ProblemSignalRepository;
import lab.wide.infrastructure.SignalRepository;
import lab.wide.infrastructure.SolutionProblemRepository;
import lab.wide.infrastructure.SolutionRepository;
import lab.wide.infrastructure.SolutionSignalRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefinementService {
  private final RefinementDetailService details;
  private final ProblemRepository problems;
  private final SolutionRepository solutions;
  private final SignalRepository signals;
  private final ProblemSignalRepository problemSignals;
  private final SolutionSignalRepository solutionSignals;
  private final SolutionProblemRepository solutionProblems;

  public RefinementService(
      ProblemRepository problems,
      SolutionRepository solutions,
      SignalRepository signals,
      ProblemSignalRepository problemSignals,
      SolutionSignalRepository solutionSignals,
      SolutionProblemRepository solutionProblems,
      RefinementDetailService details) {
    this.details = details;
    this.problems = problems;
    this.solutions = solutions;
    this.signals = signals;
    this.problemSignals = problemSignals;
    this.solutionSignals = solutionSignals;
    this.solutionProblems = solutionProblems;
  }

  public ProblemContext getProblem(UUID id) {
    var p = problem(id);
    var sources = problemSignals.findByProblemIdOrderByLinkedAtAscIdAsc(id);
    var candidates = solutionProblems.findByProblemIdOrderByLinkedAtAscIdAsc(id);
    var titles = new java.util.LinkedHashMap<UUID, String>();
    signals
        .findAllById(sources.stream().map(ProblemSignal::signalId).toList())
        .forEach(s -> titles.put(s.id(), s.title()));
    solutions
        .findAllById(candidates.stream().map(SolutionProblem::solutionId).toList())
        .forEach(s -> titles.put(s.id(), s.title()));
    return new ProblemContext(
        p,
        sources,
        candidates,
        details.questionsForProblem(id),
        details.decisionsForProblem(id),
        titles);
  }

  public SolutionContext getSolution(UUID id) {
    var s = solution(id);
    var sources = solutionSignals.findBySolutionIdOrderByLinkedAtAscIdAsc(id);
    var needs = solutionProblems.findBySolutionIdOrderByLinkedAtAscIdAsc(id);
    var titles = new java.util.LinkedHashMap<UUID, String>();
    signals
        .findAllById(sources.stream().map(SolutionSignal::signalId).toList())
        .forEach(p -> titles.put(p.id(), p.title()));
    problems
        .findAllById(needs.stream().map(SolutionProblem::problemId).toList())
        .forEach(p -> titles.put(p.id(), p.title()));
    return new SolutionContext(
        s,
        sources,
        needs,
        details.questionsForSolution(id),
        details.decisionsForSolution(id),
        titles);
  }

  public List<ProblemSignal> problemsForSignal(UUID id) {
    signal(id);
    return problemSignals.findBySignalIdOrderByLinkedAtAscIdAsc(id);
  }

  public List<SolutionSignal> solutionsForSignal(UUID id) {
    signal(id);
    return solutionSignals.findBySignalIdOrderByLinkedAtAscIdAsc(id);
  }

  private Problem problem(UUID id) {
    return problems
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Problem not found: " + id));
  }

  private Solution solution(UUID id) {
    return solutions
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Solution not found: " + id));
  }

  private void signal(UUID id) {
    if (!signals.existsById(id)) {
      throw new IllegalArgumentException("Signal not found: " + id);
    }
  }

  private static void required(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Nonblank " + field + " is required.");
    }
  }

  private static PageRequest page(Integer limit, Integer page) {
    int size = limit == null ? 50 : limit;
    int number = page == null ? 0 : page;
    if (size < 1 || size > 100 || number < 0) {
      throw new IllegalArgumentException("Limit must be 1–100 and page must be nonnegative.");
    }
    return PageRequest.of(number, size, Sort.by("updatedAt", "id").descending());
  }

  public Problem createProblem(
      String title, String description, String impact, String desiredOutcome) {
    required(title, "title");
    required(description, "description");
    var now = Instant.now();
    return problems.save(new Problem(null, title, description, impact, desiredOutcome, now, now));
  }

  @Transactional
  public Problem createProblem(
      String title,
      String description,
      String impact,
      String desiredOutcome,
      List<SignalLink> signalLinks) {
    var saved = createProblem(title, description, impact, desiredOutcome);
    if (signalLinks != null) {
      for (var link : signalLinks) {
        if (link == null || link.signalId() == null)
          throw new IllegalArgumentException("Each signal link requires a signalId and rationale.");
        linkProblemSignal(saved.id(), link.signalId(), link.rationale());
      }
    }
    return saved;
  }

  @Transactional
  public Solution createSolution(
      String title,
      String approach,
      String scope,
      String tradeoffs,
      String effort,
      List<SignalLink> signalLinks,
      List<ProblemLink> problemLinks) {
    var saved = createSolution(title, approach, scope, tradeoffs, effort);
    if (signalLinks != null) {
      for (var link : signalLinks) {
        if (link == null || link.signalId() == null)
          throw new IllegalArgumentException("Each signal link requires a signalId and rationale.");
        linkSolutionSignal(saved.id(), link.signalId(), link.rationale());
      }
    }
    if (problemLinks != null) {
      for (var link : problemLinks) {
        if (link == null || link.problemId() == null)
          throw new IllegalArgumentException(
              "Each problem link requires a problemId and rationale.");
        linkSolutionProblem(saved.id(), link.problemId(), link.rationale());
      }
    }
    return saved;
  }

  @Transactional
  public Problem editProblem(UUID id, Map<String, String> fields, List<String> clearFields) {
    fields =
        editFields(
            fields,
            clearFields,
            Set.of("title", "description", "impact", "desiredOutcome"),
            Set.of("title", "description"));
    var p = problem(id);
    return replaceProblem(
        id,
        edited(fields, "title", p.title()),
        edited(fields, "description", p.description()),
        edited(fields, "impact", p.impact()),
        edited(fields, "desiredOutcome", p.desiredOutcome()));
  }

  @Transactional
  public Solution editSolution(UUID id, Map<String, String> fields, List<String> clearFields) {
    fields =
        editFields(
            fields,
            clearFields,
            Set.of("title", "approach", "scope", "tradeoffs", "effort"),
            Set.of("title", "approach"));
    var s = solution(id);
    return replaceSolution(
        id,
        edited(fields, "title", s.title()),
        edited(fields, "approach", s.approach()),
        edited(fields, "scope", s.scope()),
        edited(fields, "tradeoffs", s.tradeoffs()),
        edited(fields, "effort", s.effort()));
  }

  private Problem replaceProblem(
      UUID id, String title, String description, String impact, String desiredOutcome) {
    required(title, "title");
    required(description, "description");
    var existing = problem(id);
    return problems.save(
        new Problem(
            id, title, description, impact, desiredOutcome, existing.createdAt(), Instant.now()));
  }

  public List<Problem> listProblems(Integer limit, Integer page) {
    return problems.findAll(page(limit, page)).getContent();
  }

  public Solution createSolution(
      String title, String approach, String scope, String tradeoffs, String effort) {
    required(title, "title");
    required(approach, "approach");
    var now = Instant.now();
    return solutions.save(new Solution(null, title, approach, scope, tradeoffs, effort, now, now));
  }

  private Solution replaceSolution(
      UUID id, String title, String approach, String scope, String tradeoffs, String effort) {
    required(title, "title");
    required(approach, "approach");
    var existing = solution(id);
    return solutions.save(
        new Solution(
            id, title, approach, scope, tradeoffs, effort, existing.createdAt(), Instant.now()));
  }

  public List<Solution> listSolutions(Integer limit, Integer page) {
    return solutions.findAll(page(limit, page)).getContent();
  }

  public ProblemSignal linkProblemSignal(UUID problemId, UUID signalId, String rationale) {
    required(rationale, "rationale");
    problem(problemId);
    signal(signalId);
    var existing = problemSignals.findByProblemIdAndSignalId(problemId, signalId);
    return problemSignals.save(
        new ProblemSignal(
            existing.map(ProblemSignal::id).orElse(null),
            problemId,
            signalId,
            rationale,
            existing.map(ProblemSignal::linkedAt).orElseGet(Instant::now)));
  }

  public ProblemSignal problemSignal(UUID id) {
    return problemSignals
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("ProblemSignal link not found: " + id));
  }

  public ProblemSignal unlinkProblemSignal(UUID id) {
    var link = problemSignal(id);
    problemSignals.deleteById(id);
    return link;
  }

  public SolutionSignal linkSolutionSignal(UUID solutionId, UUID signalId, String rationale) {
    required(rationale, "rationale");
    solution(solutionId);
    signal(signalId);
    var existing = solutionSignals.findBySolutionIdAndSignalId(solutionId, signalId);
    return solutionSignals.save(
        new SolutionSignal(
            existing.map(SolutionSignal::id).orElse(null),
            solutionId,
            signalId,
            rationale,
            existing.map(SolutionSignal::linkedAt).orElseGet(Instant::now)));
  }

  public SolutionSignal solutionSignal(UUID id) {
    return solutionSignals
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("SolutionSignal link not found: " + id));
  }

  public SolutionSignal unlinkSolutionSignal(UUID id) {
    var link = solutionSignal(id);
    solutionSignals.deleteById(id);
    return link;
  }

  public SolutionProblem linkSolutionProblem(UUID solutionId, UUID problemId, String rationale) {
    required(rationale, "rationale");
    solution(solutionId);
    problem(problemId);
    var existing = solutionProblems.findBySolutionIdAndProblemId(solutionId, problemId);
    return solutionProblems.save(
        new SolutionProblem(
            existing.map(SolutionProblem::id).orElse(null),
            solutionId,
            problemId,
            rationale,
            existing.map(SolutionProblem::linkedAt).orElseGet(Instant::now)));
  }

  public SolutionProblem solutionProblem(UUID id) {
    return solutionProblems
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("SolutionProblem link not found: " + id));
  }

  public SolutionProblem unlinkSolutionProblem(UUID id) {
    var link = solutionProblem(id);
    solutionProblems.deleteById(id);
    return link;
  }
}
