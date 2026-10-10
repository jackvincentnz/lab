package lab.wide.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lab.test.TestBase;
import lab.wide.domain.Signal;
import lab.wide.infrastructure.SignalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class SignalServiceTest extends TestBase {

  @Mock SignalRepository repository;

  @InjectMocks SignalService service;

  @Captor ArgumentCaptor<Signal> signalCaptor;

  @Captor ArgumentCaptor<Pageable> pageableCaptor;

  @Test
  void capture_savesContentAsSupplied() {
    var title = randomString();
    var content = "  " + randomString() + "\n" + randomString() + "  ";
    var source = randomString();
    when(repository.save(any(Signal.class))).thenAnswer(call -> call.getArgument(0));

    var saved = service.capture(title, content, source);

    verify(repository).save(signalCaptor.capture());
    assertThat(signalCaptor.getValue().id()).isNull();
    assertThat(saved.title()).isEqualTo(title);
    assertThat(saved.content()).isEqualTo(content);
    assertThat(saved.source()).isEqualTo(source);
    assertThat(saved.capturedAt()).isNotNull();
  }

  @Test
  void capture_storesBlankSourceAsNull() {
    when(repository.save(any(Signal.class))).thenAnswer(call -> call.getArgument(0));

    var saved = service.capture(randomString(), randomString(), " \n");

    assertThat(saved.source()).isNull();
  }

  @Test
  void capture_rejectsBlankTitle() {
    assertThatThrownBy(() -> service.capture(" \n\t", randomString(), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Nonblank title is required.");
    verifyNoInteractions(repository);
  }

  @Test
  void capture_rejectsBlankContent() {
    assertThatThrownBy(() -> service.capture(randomString(), " \n\t", null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Nonblank content is required.");
    verifyNoInteractions(repository);
  }

  @Test
  void get_rejectsUnknownId() {
    var id = UUID.randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.get(id))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Signal not found: " + id);
  }

  @Test
  void list_browsesNewestFiftyByDefault() {
    var signal = signal();
    when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(signal)));

    var listed = service.list(null, null, null);

    assertThat(listed).containsExactly(signal);
    verify(repository).findAll(pageableCaptor.capture());
    assertThat(pageableCaptor.getValue())
        .isEqualTo(PageRequest.of(0, 50, Sort.by("capturedAt", "id").descending()));
  }

  @Test
  void list_searchesTitleContentAndSource() {
    var query = randomString();
    var signal = signal();
    when(repository
            .findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrSourceContainingIgnoreCase(
                any(), any(), any(), any()))
        .thenReturn(List.of(signal));

    var listed = service.list(query, 5, 2);

    assertThat(listed).containsExactly(signal);
    verify(repository)
        .findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrSourceContainingIgnoreCase(
            query, query, query, PageRequest.of(2, 5, Sort.by("capturedAt", "id").descending()));
  }

  @Test
  void list_rejectsLimitOverOneHundred() {
    assertThatThrownBy(() -> service.list(null, 101, 0))
        .isInstanceOf(IllegalArgumentException.class);
    verifyNoInteractions(repository);
  }

  @Test
  void list_rejectsNegativePage() {
    assertThatThrownBy(() -> service.list(null, 1, -1))
        .isInstanceOf(IllegalArgumentException.class);
    verifyNoInteractions(repository);
  }

  private Signal signal() {
    return new Signal(UUID.randomUUID(), randomString(), randomString(), null, Instant.now());
  }
}
