package lab.learn.testcontainers.postgres;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import lab.test.RequiresDocker;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@RequiresDocker
public class PostgresTest {

  @Container
  public PostgreSQLContainer postgres =
      new PostgreSQLContainer(
          DockerImageName.parse(
                  "postgres:18.6@sha256:edb0c08dc33befd0262ab3b5cf1c4f3f8cfd8b09e88c05b45adee29d6761ddb6")
              .withRepository("postgres"));

  @Test
  public void executeQuery_returnsResult() throws SQLException {
    var props = new Properties();
    props.setProperty("user", postgres.getUsername());
    props.setProperty("password", postgres.getPassword());

    var connection = DriverManager.getConnection(postgres.getJdbcUrl(), props);

    var statement = connection.createStatement();
    var rs = statement.executeQuery("SELECT 1");
    rs.next();

    var result = rs.getInt(1);
    assertThat(result).isEqualTo(1);
  }
}
