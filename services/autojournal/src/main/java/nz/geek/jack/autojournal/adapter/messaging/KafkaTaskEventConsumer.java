package nz.geek.jack.autojournal.adapter.messaging;

import com.google.protobuf.Message;
import java.util.List;
import nz.geek.jack.autojournal.application.TaskEventHandler;
import nz.geek.jack.task.api.TaskAddedEvent;
import nz.geek.jack.task.api.TaskCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@KafkaListener(id = "autojournal", topics = "task")
public class KafkaTaskEventConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(KafkaTaskEventConsumer.class);

  private static final List<Class<? extends Message>> IGNORED_MESSAGES =
      List.of(TaskAddedEvent.class);

  private final TaskEventHandler taskEventHandler;

  public KafkaTaskEventConsumer(TaskEventHandler taskEventHandler) {
    this.taskEventHandler = taskEventHandler;
  }

  @KafkaHandler
  public void on(TaskCompletedEvent taskCompletedEvent) {
    LOGGER.info(String.format("Handling [%s]", taskCompletedEvent.getClass().getName()));

    taskEventHandler.handle(
        new nz.geek.jack.autojournal.application.TaskCompletedEvent(
            taskCompletedEvent.getTaskId()));
  }

  @KafkaHandler(isDefault = true)
  public void on(Message message) {
    /*
     * This is an example of making sure we aren't failing to observe and update some state from a new message that an
     * upstream producer is newly sending. It is typically a bad idea to ignore unknown messages if it means we will be
     * eventually inconsistent with the upstream state.
     *
     * An alternative is having no default handler and an empty handler for each ignored message. That would leave
     * Spring to throw when it can't find a matching handler for a known message.
     *
     * This needs to be used in conjunction with a CommonContainerStoppingErrorHandler so that the container stops
     * listening on error. E.g. we usually assume we need to fix something in this case before we start listening again.
     */
    if (IGNORED_MESSAGES.contains(message.getClass())) {
      LOGGER.debug(String.format("Ignoring [%s]", message.getClass().getName()));
    } else {
      throw new RuntimeException(
          String.format(
              "Received unknown [%s] which hasn't been ignored", message.getClass().getName()));
    }
  }
}
