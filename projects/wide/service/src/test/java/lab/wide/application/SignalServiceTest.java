package lab.wide.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lab.test.TestBase;
import lab.wide.domain.Signal;
import lab.wide.infrastructure.SignalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class SignalServiceTest extends TestBase {

  @Mock SignalRepository signalRepository;

  final Clock clock = fixedClock();

  SignalService signalService;

  @BeforeEach
  void createService() {
    signalService = new SignalService(signalRepository, clock);
  }

  @Test
  void capture_paddedContentAndSource_savesThemAsSupplied() {
    var title = randomString();
    var content = "  " + randomString() + "\n" + randomString() + "  ";
    var source = randomString();
    savesSignals();

    var signal = signalService.capture(title, content, source);

    assertThat(signal.id()).isNull();
    assertThat(signal.title()).isEqualTo(title);
    assertThat(signal.content()).isEqualTo(content);
    assertThat(signal.source()).isEqualTo(source);
    assertThat(signal.capturedAt()).isEqualTo(clock.instant());
  }

  @Test
  void capture_blankSource_savesNoSource() {
    savesSignals();

    var signal = signalService.capture(randomString(), randomString(), " \n");

    assertThat(signal.source()).isNull();
  }

  @Test
  void capture_nullSource_savesNoSource() {
    savesSignals();

    var signal = signalService.capture(randomString(), randomString(), null);

    assertThat(signal.source()).isNull();
  }

  @Test
  void capture_nullTitle_throwsWithoutSaving() {
    assertThatThrownBy(() -> signalService.capture(null, randomString(), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Nonblank title is required.");
    verifyNoInteractions(signalRepository);
  }

  @Test
  void capture_blankTitle_throwsWithoutSaving() {
    assertThatThrownBy(() -> signalService.capture(" \n\t", randomString(), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Nonblank title is required.");
    verifyNoInteractions(signalRepository);
  }

  @Test
  void capture_nullContent_throwsWithoutSaving() {
    assertThatThrownBy(() -> signalService.capture(randomString(), null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Nonblank content is required.");
    verifyNoInteractions(signalRepository);
  }

  @Test
  void capture_blankContent_throwsWithoutSaving() {
    assertThatThrownBy(() -> signalService.capture(randomString(), " \n\t", null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Nonblank content is required.");
    verifyNoInteractions(signalRepository);
  }

  @Test
  void get_existingId_returnsTheSignal() {
    var signal = signal();
    when(signalRepository.findById(signal.id())).thenReturn(Optional.of(signal));

    var found = signalService.get(signal.id());

    assertThat(found).isEqualTo(signal);
  }

  @Test
  void get_unknownId_throwsNotFound() {
    var id = UUID.randomUUID();

    assertThatThrownBy(() -> signalService.get(id))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Signal not found: " + id);
  }

  @Test
  void list_noArguments_browsesTheNewestFifty() {
    var signal = signal();
    when(signalRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(signal)));

    var signals = signalService.list(null, null, null);

    assertThat(signals).containsExactly(signal);
    verify(signalRepository)
        .findAll(PageRequest.of(0, 50, Sort.by("capturedAt", "id").descending()));
  }

  @Test
  void list_emptyQuery_browsesInsteadOfSearching() {
    when(signalRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

    signalService.list("", 5, 2);

    verify(signalRepository)
        .findAll(PageRequest.of(2, 5, Sort.by("capturedAt", "id").descending()));
  }

  @Test
  void list_query_searchesTitleContentAndSource() {
    var query = randomString();
    var signal = signal();
    when(signalRepository
            .findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrSourceContainingIgnoreCase(
                query,
                query,
                query,
                PageRequest.of(2, 5, Sort.by("capturedAt", "id").descending())))
        .thenReturn(List.of(signal));

    var signals = signalService.list(query, 5, 2);

    assertThat(signals).containsExactly(signal);
  }

  @Test
  void list_limitBelowOne_throwsWithoutReading() {
    assertThatThrownBy(() -> signalService.list(null, 0, 0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Limit must be 1–100 and page must be nonnegative.");
    verifyNoInteractions(signalRepository);
  }

  @Test
  void list_limitOverOneHundred_throwsWithoutReading() {
    assertThatThrownBy(() -> signalService.list(null, 101, 0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Limit must be 1–100 and page must be nonnegative.");
    verifyNoInteractions(signalRepository);
  }

  @Test
  void list_negativePage_throwsWithoutReading() {
    assertThatThrownBy(() -> signalService.list(null, 1, -1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Limit must be 1–100 and page must be nonnegative.");
    verifyNoInteractions(signalRepository);
  }

  private void savesSignals() {
    when(signalRepository.save(any(Signal.class))).thenAnswer(returnsFirstArg());
  }

  private Signal signal() {
    return new Signal(UUID.randomUUID(), randomString(), randomString(), null, clock.instant());
  }
}
