package lab.wide.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;
import lab.test.TestBase;
import lab.wide.application.SignalService;
import lab.wide.domain.Signal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SignalToolsTest extends TestBase {

  @Mock SignalService service;

  @InjectMocks SignalTools tools;

  @Test
  void capture_acknowledgesOnlyTheSavedId() {
    var title = randomString();
    var content = randomString();
    var source = randomString();
    var saved = new Signal(UUID.randomUUID(), title, content, source, Instant.now());
    when(service.capture(title, content, source)).thenReturn(saved);

    assertThat(tools.capture(title, content, source)).isEqualTo(new WriteReceipt(saved.id()));
  }
}
