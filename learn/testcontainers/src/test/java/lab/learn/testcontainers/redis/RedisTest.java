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
                  "redis:8.10.2-alpine@sha256:0bf6577d7774e592b29d093e3ac9390eb91e13d72c21e506afce736de5e0eeb0"))
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
