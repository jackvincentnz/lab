package nz.geek.jack.task;

import nz.geek.jack.task.adapter.messaging.KafkaTopicConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
public class TaskApplicationTest {

  // TODO: remove and add test kafka with test containers
  @MockBean KafkaTopicConfig kafkaTopicConfig;

  @Test
  public void contextLoads() {}
}
