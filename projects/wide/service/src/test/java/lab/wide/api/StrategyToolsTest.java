package lab.wide.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;
import lab.test.TestBase;
import lab.wide.application.StrategyService;
import lab.wide.domain.Strategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StrategyToolsTest extends TestBase {

  @Mock StrategyService service;

  @InjectMocks StrategyTools tools;

  @Test
  void replace_acknowledgesOnlyTheStrategyId() {
    var content = randomString();
    var saved = new Strategy(UUID.randomUUID(), content, Instant.now());
    when(service.replace(content)).thenReturn(saved);

    assertThat(tools.replace(content)).isEqualTo(new WriteReceipt(saved.id()));
  }

  @Test
  void edit_acknowledgesOnlyTheStrategyId() {
    var oldText = randomString();
    var newText = randomString();
    var saved = new Strategy(UUID.randomUUID(), newText, Instant.now());
    when(service.edit(oldText, newText)).thenReturn(saved);

    assertThat(tools.edit(oldText, newText)).isEqualTo(new WriteReceipt(saved.id()));
  }
}
