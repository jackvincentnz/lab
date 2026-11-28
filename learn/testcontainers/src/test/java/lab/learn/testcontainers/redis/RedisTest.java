package lab.learn.testcontainers.redis;

import static org.assertj.core.api.Assertions.assertThat;

import lab.test.RequiresDocker;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Testcontainers
@RequiresDocker
public class RedisTest {

  @Container
  public GenericContainer redis =
      new GenericContainer(
              DockerImageName.parse(
                  "redis:8.12.0-alpine@sha256:5af80107c1eeecb484a2269b80a26d12bfa6521787da4663bd9f19f60a163483"))
          .withExposedPorts(6379);

  @Test
  public void set_shouldSetValueForKey() {
    JedisPool pool = new JedisPool(redis.getHost(), redis.getFirstMappedPort());

    var key = "key";
    var value = "value";

    try (Jedis jedis = pool.getResource()) {
      jedis.set(key, value);

      var result = jedis.get(key);

      assertThat(value).isEqualTo(result);
    }
  }
}
