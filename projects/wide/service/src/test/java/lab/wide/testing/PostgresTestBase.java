package lab.wide.testing;

import lab.test.TestBase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/** Runs against a throwaway Postgres container, never the local WIDE database. */
@Testcontainers
public abstract class PostgresTestBase extends TestBase {

  @Container
  private static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>(
          DockerImageName.parse(
                  "postgres:17.12@sha256:b60e90042aa6b8dbecd58e224b3e36114dbf8ad754465264c2476ad58a86e45c")
              .withRepository("postgres"));

  @DynamicPropertySource
  static void database(DynamicPropertyRegistry dynamicPropertyRegistry) {
    dynamicPropertyRegistry.add("spring.datasource.url", PostgresTestBase::jdbcUrl);
    dynamicPropertyRegistry.add("spring.datasource.username", PostgresTestBase::username);
    dynamicPropertyRegistry.add("spring.datasource.password", PostgresTestBase::password);
  }

  protected static String jdbcUrl() {
    return POSTGRES.getJdbcUrl();
  }

  protected static String username() {
    return POSTGRES.getUsername();
  }

  protected static String password() {
    return POSTGRES.getPassword();
  }

  // For tests whose writes commit, so no test transaction can roll them back. Tests in a class
  // share one database, and the migration seeds the strategy row they all use.
  protected static void clearRecords(JdbcTemplate jdbcTemplate) {
    jdbcTemplate.execute(
        "TRUNCATE signal, problem, solution, problem_signal, solution_signal, solution_problem,"
            + " refinement_question, refinement_decision");
    jdbcTemplate.update("UPDATE strategy SET content = NULL, updated_at = NULL");
  }
}
