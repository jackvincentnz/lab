package nz.geek.jack.autojournal.adapter.messaging;

import com.google.protobuf.Message;
import nz.geek.jack.autojournal.application.TaskEventHandler;
import nz.geek.jack.task.api.TaskCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
public class TaskEventConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(TaskEventConsumer.class);

  private static final String LISTENER_ID = "autojournal";

  private static final String TASK_TOPIC = "task";

  private final TaskEventHandler taskEventHandler;

  public TaskEventConsumer(TaskEventHandler taskEventHandler) {
    this.taskEventHandler = taskEventHandler;
  }

  // TODO: Transactional consume
  @KafkaListener(id = LISTENER_ID, topics = TASK_TOPIC)
  public void listen(Message message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
    LOGGER.info(
        String.format(
            "Consumed event from topic %s: value = %s", topic, message.getClass().getName()));

    // TODO: move out to task event handler factory or use wrapper message with defined set of
    // events
    if (message instanceof TaskCompletedEvent m) {
      taskEventHandler.handle(
          new nz.geek.jack.autojournal.application.TaskCompletedEvent(m.getTaskId()));
    }
  }
}
