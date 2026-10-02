package lab.wide.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import lab.wide.domain.Signal;
import lab.wide.testing.ServerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

class SignalRepositoryTest extends ServerTest {

  @Autowired SignalRepository signals;

  @BeforeEach
  void clear() {
    signals.deleteAll();
  }

  @Test
  void findByText_matchesTitleContentOrSourceIgnoringCase() {
    var needle = randomString();
    var byTitle = signal(needle.toUpperCase(), randomString(), null);
    var byContent = signal(randomString(), "x " + needle + " y", null);
    var bySource = signal(randomString(), randomString(), needle);
    signal(randomString(), randomString(), randomString());

    var found =
        signals
            .findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrSourceContainingIgnoreCase(
                needle.toLowerCase(),
                needle.toLowerCase(),
                needle.toLowerCase(),
                PageRequest.of(0, 10));

    assertThat(found)
        .extracting(Signal::id)
        .containsExactlyInAnyOrder(byTitle.id(), byContent.id(), bySource.id());
  }

  @Test
  void findByText_treatsWildcardCharactersLiterally() {
    var match = signal(randomString(), "progress reads 20%", null);
    signal(randomString(), "progress reads 20 percent", null);

    var found =
        signals
            .findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrSourceContainingIgnoreCase(
                "%", "%", "%", PageRequest.of(0, 10));

    assertThat(found).extracting(Signal::id).containsExactly(match.id());
  }

  private Signal signal(String title, String content, String source) {
    return signals.save(new Signal(null, title, content, source, Instant.now()));
  }
}
