package nz.geek.jack.autojournal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.listener.CommonContainerStoppingErrorHandler;
import org.springframework.kafka.listener.CommonErrorHandler;

@SpringBootApplication
public class AutoJournalApplication {

  public static void main(String[] args) {
    SpringApplication.run(AutoJournalApplication.class, args);
  }

  @Bean
  CommonErrorHandler errorHandler() {
    /*
     * Ensure we stop listeners if something goes wrong. E.g. a downstream service call is failing or an unknown message
     * arrives that might cause eventually inconsistent state.
     *
     * This might be too heavy in its current state. E.g. we would need to restart the application if a downstream
     * service was unavailable. A better strategy might be to retry on some frequency for some exception types.
     *
     * TODO: Consider how using a DefaultErrorHandler could be used while also being eventually consistent.
     *  E.g. Moving past the failed message, with some process to catch up from state changes that were missed.
     */
    return new CommonContainerStoppingErrorHandler();
  }
}
