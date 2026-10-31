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
                  "redis:8.10.3-alpine@sha256:21090af93f8324db2f0f0f4f38a7c60369d47d917e000c3423d89d2ae2883537"))
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
