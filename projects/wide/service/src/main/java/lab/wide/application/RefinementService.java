package lab.wide.application;

import static lab.wide.application.FieldEdits.editFields;
import static lab.wide.application.FieldEdits.edited;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lab.wide.domain.Problem;
import lab.wide.domain.ProblemRepository;
import lab.wide.domain.ProblemSignal;
import lab.wide.domain.ProblemSignalRepository;
import lab.wide.domain.SignalRepository;
import lab.wide.domain.Solution;
import lab.wide.domain.SolutionProblem;
import lab.wide.domain.SolutionProblemRepository;
import lab.wide.domain.SolutionRepository;
import lab.wide.domain.SolutionSignal;
import lab.wide.domain.SolutionSignalRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefinementService {
  private final ProblemRepository problemRepository;
  private final SolutionRepository solutionRepository;
  private final SignalRepository signalRepository;
  private final ProblemSignalRepository problemSignalRepository;
  private final SolutionSignalRepository solutionSignalRepository;
  private final SolutionProblemRepository solutionProblemRepository;
  private final RefinementDetailService refinementDetailService;
  private final Clock clock;

  public RefinementService(
      ProblemRepository problemRepository,
      SolutionRepository solutionRepository,
      SignalRepository signalRepository,
      ProblemSignalRepository problemSignalRepository,
      SolutionSignalRepository solutionSignalRepository,
      SolutionProblemRepository solutionProblemRepository,
      RefinementDetailService refinementDetailService,
      Clock clock) {
    this.problemRepository = problemRepository;
    this.solutionRepository = solutionRepository;
    this.signalRepository = signalRepository;
    this.problemSignalRepository = problemSignalRepository;
    this.solutionSignalRepository = solutionSignalRepository;
    this.solutionProblemRepository = solutionProblemRepository;
    this.refinementDetailService = refinementDetailService;
    this.clock = clock;
  }

  public ProblemContext getProblem(UUID id) {
    var problem = problem(id);
    var problemSignals = problemSignalRepository.findByProblemIdOrderByLinkedAtAscIdAsc(id);
    var solutionProblems = solutionProblemRepository.findByProblemIdOrderByLinkedAtAscIdAsc(id);

    var linkedTitles = new LinkedHashMap<UUID, String>();
    signalRepository
        .findAllById(problemSignals.stream().map(ProblemSignal::signalId).toList())
        .forEach(signal -> linkedTitles.put(signal.id(), signal.title()));
    solutionRepository
        .findAllById(solutionProblems.stream().map(SolutionProblem::solutionId).toList())
        .forEach(solution -> linkedTitles.put(solution.id(), solution.title()));

    return new ProblemContext(
        problem,
        problemSignals,
        solutionProblems,
        refinementDetailService.questionsForProblem(id),
        refinementDetailService.decisionsForProblem(id),
        linkedTitles);
  }

  private Problem problem(UUID id) {
    return findProblem(id)
        .orElseThrow(() -> new IllegalArgumentException("Problem not found: " + id));
  }

  public Optional<Problem> findProblem(UUID id) {
    return problemRepository.findById(id);
  }

  public SolutionContext getSolution(UUID id) {
    var solution = solution(id);
    var solutionSignals = solutionSignalRepository.findBySolutionIdOrderByLinkedAtAscIdAsc(id);
    var solutionProblems = solutionProblemRepository.findBySolutionIdOrderByLinkedAtAscIdAsc(id);

    var linkedTitles = new LinkedHashMap<UUID, String>();
    signalRepository
        .findAllById(solutionSignals.stream().map(SolutionSignal::signalId).toList())
        .forEach(signal -> linkedTitles.put(signal.id(), signal.title()));
    problemRepository
        .findAllById(solutionProblems.stream().map(SolutionProblem::problemId).toList())
        .forEach(problem -> linkedTitles.put(problem.id(), problem.title()));

    return new SolutionContext(
        solution,
        solutionSignals,
        solutionProblems,
        refinementDetailService.questionsForSolution(id),
        refinementDetailService.decisionsForSolution(id),
        linkedTitles);
  }

  private Solution solution(UUID id) {
    return findSolution(id)
        .orElseThrow(() -> new IllegalArgumentException("Solution not found: " + id));
  }

  public Optional<Solution> findSolution(UUID id) {
    return solutionRepository.findById(id);
  }

  public List<ProblemSignal> problemsForSignal(UUID id) {
    signal(id);

    return problemSignalRepository.findBySignalIdOrderByLinkedAtAscIdAsc(id);
  }

  private void signal(UUID id) {
    if (!signalRepository.existsById(id)) {
      throw new IllegalArgumentException("Signal not found: " + id);
    }
  }

  public List<SolutionSignal> solutionsForSignal(UUID id) {
    signal(id);

    return solutionSignalRepository.findBySignalIdOrderByLinkedAtAscIdAsc(id);
  }

  public List<Problem> listProblems(Integer limit, Integer page) {
    return problemRepository.findAll(page(limit, page)).getContent();
  }

  private static PageRequest page(Integer limit, Integer page) {
    int size = limit == null ? 50 : limit;
    int number = page == null ? 0 : page;
    if (size < 1 || size > 100 || number < 0) {
      throw new IllegalArgumentException("Limit must be 1–100 and page must be nonnegative.");
    }

    return PageRequest.of(number, size, Sort.by("updatedAt", "id").descending());
  }

  public List<Solution> listSolutions(Integer limit, Integer page) {
    return solutionRepository.findAll(page(limit, page)).getContent();
  }

  public Problem createProblem(
      String title, String description, String impact, String desiredOutcome) {
    required(title, "title");
    required(description, "description");

    var now = Instant.now(clock);
    return problemRepository.save(
        new Problem(null, title, description, impact, desiredOutcome, now, now));
  }

  private static void required(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Nonblank " + field + " is required.");
    }
  }

  @Transactional
  public Problem createProblem(
      String title,
      String description,
      String impact,
      String desiredOutcome,
      List<SignalLink> signalLinks) {
    var problem = createProblem(title, description, impact, desiredOutcome);

    for (var signalLink : orEmpty(signalLinks)) {
      if (signalLink == null || signalLink.signalId() == null) {
        throw new IllegalArgumentException("Each signal link requires a signalId and rationale.");
      }
      linkProblemSignal(problem.id(), signalLink.signalId(), signalLink.rationale());
    }

    return problem;
  }

  private static <T> List<T> orEmpty(List<T> links) {
    return links == null ? List.of() : links;
  }

  public Solution createSolution(
      String title, String approach, String scope, String tradeoffs, String effort) {
    required(title, "title");
    required(approach, "approach");

    var now = Instant.now(clock);
    return solutionRepository.save(
        new Solution(null, title, approach, scope, tradeoffs, effort, now, now));
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
    var solution = createSolution(title, approach, scope, tradeoffs, effort);

    for (var signalLink : orEmpty(signalLinks)) {
      if (signalLink == null || signalLink.signalId() == null) {
        throw new IllegalArgumentException("Each signal link requires a signalId and rationale.");
      }
      linkSolutionSignal(solution.id(), signalLink.signalId(), signalLink.rationale());
    }

    for (var problemLink : orEmpty(problemLinks)) {
      if (problemLink == null || problemLink.problemId() == null) {
        throw new IllegalArgumentException("Each problem link requires a problemId and rationale.");
      }
      linkSolutionProblem(solution.id(), problemLink.problemId(), problemLink.rationale());
    }

    return solution;
  }

  @Transactional
  public Problem editProblem(UUID id, Map<String, String> fields, List<String> clearFields) {
    var changes =
        editFields(
            fields,
            clearFields,
            Set.of("title", "description", "impact", "desiredOutcome"),
            Set.of("title", "description"));
    var problem = problem(id);

    var title = edited(changes, "title", problem.title());
    var description = edited(changes, "description", problem.description());
    required(title, "title");
    required(description, "description");

    return problemRepository.save(
        new Problem(
            id,
            title,
            description,
            edited(changes, "impact", problem.impact()),
            edited(changes, "desiredOutcome", problem.desiredOutcome()),
            problem.createdAt(),
            Instant.now(clock)));
  }

  @Transactional
  public Solution editSolution(UUID id, Map<String, String> fields, List<String> clearFields) {
    var changes =
        editFields(
            fields,
            clearFields,
            Set.of("title", "approach", "scope", "tradeoffs", "effort"),
            Set.of("title", "approach"));
    var solution = solution(id);

    var title = edited(changes, "title", solution.title());
    var approach = edited(changes, "approach", solution.approach());
    required(title, "title");
    required(approach, "approach");

    return solutionRepository.save(
        new Solution(
            id,
            title,
            approach,
            edited(changes, "scope", solution.scope()),
            edited(changes, "tradeoffs", solution.tradeoffs()),
            edited(changes, "effort", solution.effort()),
            solution.createdAt(),
            Instant.now(clock)));
  }

  public ProblemSignal linkProblemSignal(UUID problemId, UUID signalId, String rationale) {
    required(rationale, "rationale");
    problem(problemId);
    signal(signalId);

    var existing = problemSignalRepository.findByProblemIdAndSignalId(problemId, signalId);

    return problemSignalRepository.save(
        new ProblemSignal(
            existing.map(ProblemSignal::id).orElse(null),
            problemId,
            signalId,
            rationale,
            existing.map(ProblemSignal::linkedAt).orElseGet(() -> Instant.now(clock))));
  }

  public ProblemSignal problemSignal(UUID id) {
    return problemSignalRepository
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("ProblemSignal link not found: " + id));
  }

  public ProblemSignal unlinkProblemSignal(UUID id) {
    var problemSignal = problemSignal(id);

    problemSignalRepository.deleteById(id);

    return problemSignal;
  }

  public SolutionSignal linkSolutionSignal(UUID solutionId, UUID signalId, String rationale) {
    required(rationale, "rationale");
    solution(solutionId);
    signal(signalId);

    var existing = solutionSignalRepository.findBySolutionIdAndSignalId(solutionId, signalId);

    return solutionSignalRepository.save(
        new SolutionSignal(
            existing.map(SolutionSignal::id).orElse(null),
            solutionId,
            signalId,
            rationale,
            existing.map(SolutionSignal::linkedAt).orElseGet(() -> Instant.now(clock))));
  }

  public SolutionSignal solutionSignal(UUID id) {
    return solutionSignalRepository
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("SolutionSignal link not found: " + id));
  }

  public SolutionSignal unlinkSolutionSignal(UUID id) {
    var solutionSignal = solutionSignal(id);

    solutionSignalRepository.deleteById(id);

    return solutionSignal;
  }

  public SolutionProblem linkSolutionProblem(UUID solutionId, UUID problemId, String rationale) {
    required(rationale, "rationale");
    solution(solutionId);
    problem(problemId);

    var existing = solutionProblemRepository.findBySolutionIdAndProblemId(solutionId, problemId);

    return solutionProblemRepository.save(
        new SolutionProblem(
            existing.map(SolutionProblem::id).orElse(null),
            solutionId,
            problemId,
            rationale,
            existing.map(SolutionProblem::linkedAt).orElseGet(() -> Instant.now(clock))));
  }

  public SolutionProblem solutionProblem(UUID id) {
    return solutionProblemRepository
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("SolutionProblem link not found: " + id));
  }

  public SolutionProblem unlinkSolutionProblem(UUID id) {
    var solutionProblem = solutionProblem(id);

    solutionProblemRepository.deleteById(id);

    return solutionProblem;
  }
}
