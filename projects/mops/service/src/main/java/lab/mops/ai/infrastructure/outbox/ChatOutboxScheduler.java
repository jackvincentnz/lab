package lab.mops.ai.infrastructure.outbox;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class ChatOutboxScheduler {
  private final ChatOutboxWorker worker;

  public ChatOutboxScheduler(ChatOutboxWorker worker) {
    this.worker = worker;
  }

  @Scheduled(fixedDelay = 1000)
  public void deliver() {
    // Bound each sweep so the scheduler can service other scheduled tasks.
    for (var count = 0; count < 20 && worker.processNext(); count++) {}
  }
}
