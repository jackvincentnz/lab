package lab.wide.functional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;
import lab.wide.application.ProblemLink;
import lab.wide.application.RefinementService;
import lab.wide.application.SignalLink;
import lab.wide.application.SignalService;
import lab.wide.infrastructure.ProblemRepository;
import lab.wide.infrastructure.ProblemSignalRepository;
import lab.wide.infrastructure.SignalRepository;
import lab.wide.infrastructure.SolutionProblemRepository;
import lab.wide.infrastructure.SolutionRepository;
import lab.wide.infrastructure.SolutionSignalRepository;
import lab.wide.testing.ServerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** Transactional behaviour that only a real database can demonstrate. */
class RefinementFunctionalTest extends ServerTest {

  @Autowired RefinementService refinement;
  @Autowired SignalService intake;
  @Autowired SignalRepository signals;
  @Autowired ProblemRepository problems;
  @Autowired SolutionRepository solutions;
  @Autowired ProblemSignalRepository problemSignals;
  @Autowired SolutionSignalRepository solutionSignals;
  @Autowired SolutionProblemRepository solutionProblems;

  @BeforeEach
  void clear() {
    solutionProblems.deleteAll();
    solutionSignals.deleteAll();
    problemSignals.deleteAll();
    solutions.deleteAll();
    problems.deleteAll();
    signals.deleteAll();
  }

  @Test
  void createProblem_rollsBackTheDraftWhenALinkFails() {
    var signal = intake.capture(randomString(), randomString(), null);
    var links =
        List.of(
            new SignalLink(signal.id(), randomString()),
            new SignalLink(UUID.randomUUID(), randomString()));

    assertThatThrownBy(
            () -> refinement.createProblem(randomString(), randomString(), null, null, links))
        .hasMessageContaining("Signal not found");

    assertThat(problems.count()).isZero();
    assertThat(problemSignals.count()).isZero();
  }

  @Test
  void createSolution_rollsBackTheDraftAndItsSourceLinksWhenAProblemLinkFails() {
    var signal = intake.capture(randomString(), randomString(), null);
    var problem = refinement.createProblem(randomString(), randomString(), null, null);

    assertThatThrownBy(
            () ->
                refinement.createSolution(
                    randomString(),
                    randomString(),
                    null,
                    null,
                    null,
                    List.of(new SignalLink(signal.id(), randomString())),
                    List.of(
                        new ProblemLink(problem.id(), randomString()),
                        new ProblemLink(UUID.randomUUID(), randomString()))))
        .hasMessageContaining("Problem not found");

    assertThat(solutions.count()).isZero();
    assertThat(solutionSignals.count()).isZero();
    assertThat(solutionProblems.count()).isZero();
  }
}
