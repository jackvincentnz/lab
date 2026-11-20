package lab.wide.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;
import lab.test.RequiresDocker;
import lab.wide.domain.ProblemRepository;
import lab.wide.domain.ProblemSignalRepository;
import lab.wide.domain.SolutionProblemRepository;
import lab.wide.domain.SolutionRepository;
import lab.wide.domain.SolutionSignalRepository;
import lab.wide.testing.PostgresTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

/** Rollback of a draft and its links, which only a real transaction demonstrates. */
@SpringBootTest
@RequiresDocker
class RefinementServiceTransactionTest extends PostgresTestBase {

  @Autowired RefinementService refinementService;

  @Autowired SignalService signalService;

  @Autowired ProblemRepository problemRepository;

  @Autowired SolutionRepository solutionRepository;

  @Autowired ProblemSignalRepository problemSignalRepository;

  @Autowired SolutionSignalRepository solutionSignalRepository;

  @Autowired SolutionProblemRepository solutionProblemRepository;

  @Autowired JdbcTemplate jdbcTemplate;

  @BeforeEach
  void clear() {
    clearRecords(jdbcTemplate);
  }

  @Test
  void createProblem_unknownSignalLink_rollsBackTheDraftAndEarlierLinks() {
    var signal = signalService.capture(randomString(), randomString(), null);
    var signalLinks =
        List.of(
            new SignalLink(signal.id(), randomString()),
            new SignalLink(UUID.randomUUID(), randomString()));

    assertThatThrownBy(
            () ->
                refinementService.createProblem(
                    randomString(), randomString(), null, null, signalLinks))
        .hasMessageContaining("Signal not found");

    assertThat(problemRepository.count()).isZero();
    assertThat(problemSignalRepository.count()).isZero();
  }

  @Test
  void createSolution_unknownProblemLink_rollsBackTheDraftAndEarlierLinks() {
    var signal = signalService.capture(randomString(), randomString(), null);
    var problem = refinementService.createProblem(randomString(), randomString(), null, null);
    var signalLinks = List.of(new SignalLink(signal.id(), randomString()));
    var problemLinks =
        List.of(
            new ProblemLink(problem.id(), randomString()),
            new ProblemLink(UUID.randomUUID(), randomString()));

    assertThatThrownBy(
            () ->
                refinementService.createSolution(
                    randomString(), randomString(), null, null, null, signalLinks, problemLinks))
        .hasMessageContaining("Problem not found");

    assertThat(solutionRepository.count()).isZero();
    assertThat(solutionSignalRepository.count()).isZero();
    assertThat(solutionProblemRepository.count()).isZero();
  }
}
