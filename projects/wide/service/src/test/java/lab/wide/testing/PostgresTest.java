package lab.wide.testing;

import lab.test.TestBase;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Runs against a throwaway Postgres container, never the local WIDE database. */
@Testcontainers
public abstract class PostgresTest extends TestBase {

  @Container
  private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17");

  protected static String jdbcUrl() {
    return POSTGRES.getJdbcUrl();
  }

  protected static String username() {
    return POSTGRES.getUsername();
  }

  protected static String password() {
    return POSTGRES.getPassword();
  }
}
