package lab.wide.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lab.wide.testing.PostgresTest;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.junit.jupiter.api.Test;

/** Applies the shipped migrations to earlier schema versions holding representative data. */
class MigrationTest extends PostgresTest {

  private static final List<String> RECORD_TABLES =
      List.of(
          "signal",
          "problem",
          "solution",
          "strategy",
          "problem_signal",
          "solution_signal",
          "solution_problem");

  @Test
  void migrate_preservesSignalsAndSeedsASingleStrategyRow() throws SQLException {
    var schema = "upgrade_" + randomString().toLowerCase();
    var flyway = flyway(schema);
    flyway.target("1").load().migrate();
    try (var connection = connect(schema);
        var sql = connection.createStatement()) {
      var title = randomString();
      var content = randomString();
      sql.executeUpdate(
          "INSERT INTO signal (title, content, source, captured_at) VALUES ('"
              + title
              + "','"
              + content
              + "','Fixture','2026-01-01T00:00:00Z')");
      var before = snapshot(sql, "signal");

      flyway.target("2").load().migrate();
      var strategy = randomString();
      sql.executeUpdate(
          "UPDATE strategy SET content = '"
              + strategy
              + "', updated_at = '2026-01-02T00:00:00Z' WHERE id = 1");
      flyway.target("latest").load().migrate();

      assertThat(snapshot(sql, "signal")).isEqualTo(before);
      try (var rows = sql.executeQuery("SELECT id::text, content FROM strategy")) {
        assertThat(rows.next()).isTrue();
        assertThat(rows.getString(1)).hasSize(36);
        assertThat(rows.getString(2)).isEqualTo(strategy);
        assertThat(rows.next()).isFalse();
      }
      assertThatThrownBy(() -> sql.executeUpdate("INSERT INTO strategy DEFAULT VALUES"))
          .isInstanceOf(SQLException.class)
          .hasMessageContaining("strategy_singleton_idx");
    }
  }

  @Test
  void migrate_dropsTheSignalKindWithoutChangingRecordsOrInferringDetails() throws SQLException {
    var schema = "upgrade_" + randomString().toLowerCase();
    var flyway = flyway(schema);
    flyway.target("5").load().migrate();
    try (var connection = connect(schema);
        var sql = connection.createStatement()) {
      sql.executeUpdate(
          "INSERT INTO signal(title,content,source,kind,captured_at) VALUES"
              + " ('Original','  Input  ','Fixture','ORIGINAL',now()),"
              + " ('Analysis','Uncertain conclusion','Fixture','ANALYSIS',now())");
      sql.executeUpdate(
          "INSERT INTO problem(title,description,open_questions,created_at,updated_at) VALUES"
              + " ('Need','Description','Question? Keep this wording.',now(),now())");
      sql.executeUpdate(
          "INSERT INTO solution(title,approach,open_questions,created_at,updated_at) VALUES"
              + " ('Option','Proposed approach','Park until the owner decides.',now(),now())");
      linkEverything(sql);
      var before = snapshot(sql, RECORD_TABLES, "to_jsonb(t) - 'kind'");

      flyway.target("6").load().migrate();

      assertThat(snapshot(sql, RECORD_TABLES, "to_jsonb(t)")).isEqualTo(before);
      assertThat(columnCount(sql, schema, "kind")).isZero();
      assertThat(count(sql, "refinement_question")).isZero();
      assertThat(count(sql, "refinement_decision")).isZero();
    }
  }

  @Test
  void migrate_appendsEarlierQuestionsToContentVerbatim() throws SQLException {
    var schema = "upgrade_" + randomString().toLowerCase();
    var flyway = flyway(schema);
    flyway.target("6").load().migrate();
    try (var connection = connect(schema);
        var sql = connection.createStatement()) {
      sql.executeUpdate(
          "INSERT INTO problem(title,description,open_questions,created_at,updated_at) VALUES"
              + " ('Need','Original description','  Question?\nUnverified note.  ',now(),now()),"
              + " ('Empty','Empty notes','',now(),now()),"
              + " ('Unknown','Absent notes',NULL,now(),now()),"
              + " ('Space','Whitespace notes','  ',now(),now())");
      sql.executeUpdate(
          "INSERT INTO solution(title,approach,open_questions,created_at,updated_at) VALUES"
              + " ('Option','Original approach','[Reference](https://example.com)\n"
              + "Decision recorded elsewhere.',now(),now())");
      sql.executeUpdate(
          "INSERT INTO signal(title,content,captured_at) VALUES ('Source','Unchanged',now())");
      linkEverything(sql);
      sql.executeUpdate(
          "INSERT INTO refinement_question(problem_id,question,status,answer,created_at,updated_at)"
              + " SELECT id,'Who?','ANSWERED','Demo visitors',now(),now() FROM problem LIMIT 1");
      sql.executeUpdate(
          "INSERT INTO refinement_decision(solution_id,decision,source,created_at,updated_at)"
              + " SELECT id,'Compare options','Demo owner',now(),now() FROM solution");
      var tables = new java.util.ArrayList<>(RECORD_TABLES);
      tables.addAll(List.of("refinement_question", "refinement_decision"));
      var expected = new HashMap<String, String>();
      for (var table : tables) {
        expected.put(table, snapshot(sql, table, appendedQuestions(table)));
      }

      flyway.target("latest").load().migrate();

      for (var table : tables) {
        assertThat(snapshot(sql, table)).as(table).isEqualTo(expected.get(table));
      }
      assertThat(columnCount(sql, schema, "open_questions")).isZero();
    }
  }

  private static FluentConfiguration flyway(String schema) {
    return Flyway.configure()
        .dataSource(jdbcUrl(), username(), password())
        .schemas(schema)
        .defaultSchema(schema);
  }

  private static Connection connect(String schema) throws SQLException {
    var connection = DriverManager.getConnection(jdbcUrl(), username(), password());
    connection.setSchema(schema);
    return connection;
  }

  private static void linkEverything(Statement sql) throws SQLException {
    sql.executeUpdate(
        "INSERT INTO problem_signal(problem_id,signal_id,rationale,linked_at) SELECT"
            + " p.id,s.id,'Context',now() FROM problem p CROSS JOIN signal s");
    sql.executeUpdate(
        "INSERT INTO solution_signal(solution_id,signal_id,rationale,linked_at) SELECT"
            + " p.id,s.id,'Source',now() FROM solution p CROSS JOIN signal s");
    sql.executeUpdate(
        "INSERT INTO solution_problem(solution_id,problem_id,rationale,linked_at) SELECT"
            + " s.id,p.id,'Contribution',now() FROM solution s CROSS JOIN problem p");
  }

  // The expected shape after V7: earlier free text appended to the main field, then dropped.
  private static String appendedQuestions(String table) {
    if (!table.equals("problem") && !table.equals("solution")) {
      return "to_jsonb(t)";
    }
    var field = table.equals("problem") ? "description" : "approach";
    return "(to_jsonb(t) - 'open_questions') || jsonb_build_object('"
        + field
        + "', "
        + field
        + " || CASE WHEN open_questions IS NULL OR open_questions = '' THEN ''"
        + " ELSE E'\\n\\n### Earlier refinement context\\n\\n' || open_questions END)";
  }

  private static String snapshot(Statement sql, String table) throws SQLException {
    return snapshot(sql, table, "to_jsonb(t)");
  }

  private static String snapshot(Statement sql, String table, String expression)
      throws SQLException {
    try (var rows =
        sql.executeQuery(
            "SELECT jsonb_agg(" + expression + " ORDER BY id)::text FROM " + table + " t")) {
      rows.next();
      return rows.getString(1);
    }
  }

  private static Map<String, String> snapshot(Statement sql, List<String> tables, String expression)
      throws SQLException {
    var result = new HashMap<String, String>();
    for (var table : tables) {
      result.put(table, snapshot(sql, table, expression));
    }
    return result;
  }

  private static int columnCount(Statement sql, String schema, String column) throws SQLException {
    try (var rows =
        sql.executeQuery(
            "SELECT count(*) FROM information_schema.columns WHERE table_schema='"
                + schema
                + "' AND column_name='"
                + column
                + "'")) {
      rows.next();
      return rows.getInt(1);
    }
  }

  private static int count(Statement sql, String table) throws SQLException {
    try (var rows = sql.executeQuery("SELECT count(*) FROM " + table)) {
      rows.next();
      return rows.getInt(1);
    }
  }
}
