package lab.wide.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import lab.test.RequiresDocker;
import lab.wide.testing.PostgresTestBase;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

@RequiresDocker
class MigrationTest extends PostgresTestBase {

  @Test
  void migrate_emptyDatabase_seedsOneUnsetStrategy() throws SQLException {
    var schema = randomSchema();

    migrate(schema);

    try (var connection = connect(schema);
        var statement = connection.createStatement();
        var resultSet = statement.executeQuery("SELECT content, updated_at FROM strategy")) {
      assertThat(resultSet.next()).isTrue();
      assertThat(resultSet.getString("content")).isNull();
      assertThat(resultSet.getTimestamp("updated_at")).isNull();
      assertThat(resultSet.next()).isFalse();
    }
  }

  @Test
  void insertStrategy_afterMigration_isRejectedAsASecondRow() throws SQLException {
    var schema = randomSchema();
    migrate(schema);

    try (var connection = connect(schema);
        var statement = connection.createStatement()) {

      assertThatThrownBy(() -> statement.executeUpdate("INSERT INTO strategy DEFAULT VALUES"))
          .isInstanceOf(SQLException.class)
          .hasMessageContaining("strategy_singleton_idx");
    }
  }

  @Test
  void insertProblemSignal_samePairTwice_isRejected() throws SQLException {
    try (var connection = connectWithRecords();
        var statement = connection.createStatement()) {

      assertThatThrownBy(
              () ->
                  statement.executeUpdate(
                      "INSERT INTO problem_signal (problem_id, signal_id, rationale, linked_at)"
                          + " SELECT problem.id, signal.id, 'rationale', now()"
                          + " FROM problem, signal, generate_series(1, 2)"))
          .isInstanceOf(SQLException.class)
          .hasMessageContaining("\"problem_signal_problem_id_signal_id_key\"");
    }
  }

  @Test
  void insertSolutionSignal_samePairTwice_isRejected() throws SQLException {
    try (var connection = connectWithRecords();
        var statement = connection.createStatement()) {

      assertThatThrownBy(
              () ->
                  statement.executeUpdate(
                      "INSERT INTO solution_signal (solution_id, signal_id, rationale, linked_at)"
                          + " SELECT solution.id, signal.id, 'rationale', now()"
                          + " FROM solution, signal, generate_series(1, 2)"))
          .isInstanceOf(SQLException.class)
          .hasMessageContaining("\"solution_signal_solution_id_signal_id_key\"");
    }
  }

  @Test
  void insertSolutionProblem_samePairTwice_isRejected() throws SQLException {
    try (var connection = connectWithRecords();
        var statement = connection.createStatement()) {

      assertThatThrownBy(
              () ->
                  statement.executeUpdate(
                      "INSERT INTO solution_problem (solution_id, problem_id, rationale, linked_at)"
                          + " SELECT solution.id, problem.id, 'rationale', now()"
                          + " FROM solution, problem, generate_series(1, 2)"))
          .isInstanceOf(SQLException.class)
          .hasMessageContaining("\"solution_problem_solution_id_problem_id_key\"");
    }
  }

  @Test
  void insertRefinementQuestion_bothParents_isRejected() throws SQLException {
    try (var connection = connectWithRecords();
        var statement = connection.createStatement()) {

      assertThatThrownBy(
              () ->
                  statement.executeUpdate(
                      "INSERT INTO refinement_question"
                          + " (problem_id, solution_id, question, status, created_at, updated_at)"
                          + " SELECT problem.id, solution.id, 'question', 'OPEN', now(), now()"
                          + " FROM problem, solution"))
          .isInstanceOf(SQLException.class)
          .hasMessageContaining("\"refinement_question_check\"");
    }
  }

  @Test
  void insertRefinementDecision_noParent_isRejected() throws SQLException {
    try (var connection = connectWithRecords();
        var statement = connection.createStatement()) {

      assertThatThrownBy(
              () ->
                  statement.executeUpdate(
                      "INSERT INTO refinement_decision (decision, created_at, updated_at)"
                          + " VALUES ('decision', now(), now())"))
          .isInstanceOf(SQLException.class)
          .hasMessageContaining("\"refinement_decision_check\"");
    }
  }

  private Connection connectWithRecords() throws SQLException {
    var schema = randomSchema();
    migrate(schema);

    var connection = connect(schema);
    try (var statement = connection.createStatement()) {
      statement.execute(
          "INSERT INTO signal (title, content, captured_at) VALUES ('title', 'content', now());"
              + " INSERT INTO problem (title, description, created_at, updated_at)"
              + " VALUES ('title', 'description', now(), now());"
              + " INSERT INTO solution (title, approach, created_at, updated_at)"
              + " VALUES ('title', 'approach', now(), now())");
    }
    return connection;
  }

  private String randomSchema() {
    return "migration_" + randomString().toLowerCase();
  }

  private static void migrate(String schema) {
    Flyway.configure()
        .dataSource(jdbcUrl(), username(), password())
        .schemas(schema)
        .defaultSchema(schema)
        .load()
        .migrate();
  }

  private static Connection connect(String schema) throws SQLException {
    var connection = DriverManager.getConnection(jdbcUrl(), username(), password());
    connection.setSchema(schema);
    return connection;
  }
}
