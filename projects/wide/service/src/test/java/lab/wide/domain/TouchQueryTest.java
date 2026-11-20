package lab.wide.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lab.test.RequiresDocker;
import lab.wide.testing.PostgresTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@RequiresDocker
class TouchQueryTest extends PostgresTestBase {

  @Autowired ProblemRepository problemRepository;

  @Autowired SolutionRepository solutionRepository;

  final Instant updatedAt = fixedClock().instant().truncatedTo(ChronoUnit.MICROS);

  @Test
  void touch_problem_changesOnlyItsUpdateTime() {
    var problem = problem();
    var otherProblem = problem();

    problemRepository.touch(problem.id(), updatedAt);

    assertThat(problemRepository.findById(problem.id()))
        .contains(
            new Problem(
                problem.id(),
                problem.title(),
                problem.description(),
                problem.impact(),
                problem.desiredOutcome(),
                problem.createdAt(),
                updatedAt));
    assertThat(problemRepository.findById(otherProblem.id())).contains(otherProblem);
  }

  @Test
  void touch_solution_changesOnlyItsUpdateTime() {
    var solution = solution();
    var otherSolution = solution();

    solutionRepository.touch(solution.id(), updatedAt);

    assertThat(solutionRepository.findById(solution.id()))
        .contains(
            new Solution(
                solution.id(),
                solution.title(),
                solution.approach(),
                solution.scope(),
                solution.tradeoffs(),
                solution.effort(),
                solution.createdAt(),
                updatedAt));
    assertThat(solutionRepository.findById(otherSolution.id())).contains(otherSolution);
  }

  // Read back so the expected values carry the precision Postgres stored.
  private Problem problem() {
    var createdAt = updatedAt.minus(1, ChronoUnit.DAYS);
    var problem =
        problemRepository.save(
            new Problem(
                null,
                randomString(),
                randomString(),
                randomString(),
                randomString(),
                createdAt,
                createdAt));
    return problemRepository.findById(problem.id()).orElseThrow();
  }

  private Solution solution() {
    var createdAt = updatedAt.minus(1, ChronoUnit.DAYS);
    var solution =
        solutionRepository.save(
            new Solution(
                null,
                randomString(),
                randomString(),
                randomString(),
                randomString(),
                randomString(),
                createdAt,
                createdAt));
    return solutionRepository.findById(solution.id()).orElseThrow();
  }
}
