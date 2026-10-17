package lab.wide.testing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/** Boots the service on a random port against the test container's migrated schema. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class ServerTest extends PostgresTest {

  @DynamicPropertySource
  static void database(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", PostgresTest::jdbcUrl);
    registry.add("spring.datasource.username", PostgresTest::username);
    registry.add("spring.datasource.password", PostgresTest::password);
  }

  @Autowired private Environment environment;

  protected String baseUrl() {
    return "http://127.0.0.1:" + environment.getProperty("local.server.port");
  }
}
