package lab.mops.ai.infrastructure.outbox;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import lab.test.TestBase;
import org.junit.jupiter.api.Test;

class ChatOutboxSchedulerTest extends TestBase {
  @Test
  void deliver_emptyQueue_stopsSweep() {
    var worker = mock(ChatOutboxWorker.class);
    new ChatOutboxScheduler(worker).deliver();
    verify(worker).processNext();
  }

  @Test
  void deliver_busyQueue_boundsSweep() {
    var worker = mock(ChatOutboxWorker.class);
    when(worker.processNext()).thenReturn(true);
    new ChatOutboxScheduler(worker).deliver();
    verify(worker, times(20)).processNext();
  }
}
