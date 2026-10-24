package lab.wide.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lab.test.TestBase;
import lab.wide.domain.Strategy;
import lab.wide.infrastructure.StrategyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StrategyServiceTest extends TestBase {

  @Mock StrategyRepository repository;

  @InjectMocks StrategyService service;

  @Test
  void read_failsWhenMigrationsHaveNotSeededTheRow() {
    when(repository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.read()).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void replace_savesContentOnTheExistingRow() {
    var id = unset();
    var content = "  " + randomString() + "\n" + randomString() + "  ";
    when(repository.save(any(Strategy.class))).thenAnswer(call -> call.getArgument(0));

    var saved = service.replace(content);

    assertThat(saved.id()).isEqualTo(id);
    assertThat(saved.content()).isEqualTo(content);
    assertThat(saved.updatedAt()).isNotNull();
  }

  @Test
  void replace_rejectsBlankContent() {
    unset();

    assertThatThrownBy(() -> service.replace(" \n\t"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Nonblank strategy content is required.");
    verify(repository, never()).save(any());
  }

  @Test
  void edit_replacesTheOnlyOccurrence() {
    var prefix = randomString();
    var suffix = randomString();
    var replacement = randomString();
    var id = current(prefix + " keep short " + suffix);
    when(repository.save(any(Strategy.class))).thenAnswer(call -> call.getArgument(0));

    var edited = service.edit("keep short", replacement);

    assertThat(edited.id()).isEqualTo(id);
    assertThat(edited.content()).isEqualTo(prefix + " " + replacement + " " + suffix);
  }

  @Test
  void edit_removesThePassageWhenNewTextIsEmpty() {
    var prefix = randomString();
    current(prefix + "\n- remove me");
    when(repository.save(any(Strategy.class))).thenAnswer(call -> call.getArgument(0));

    var edited = service.edit("\n- remove me", "");

    assertThat(edited.content()).isEqualTo(prefix);
  }

  @Test
  void edit_rejectsEmptyOldText() {
    assertThatThrownBy(() -> service.edit("", randomString()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Nonempty oldText is required.");
  }

  @Test
  void edit_rejectsNullNewText() {
    assertThatThrownBy(() -> service.edit(randomString(), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("newText is required; use an empty string to remove a passage.");
  }

  @Test
  void edit_rejectsWhenNoStrategyIsSet() {
    unset();

    assertThatThrownBy(() -> service.edit(randomString(), randomString()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("No strategy is set. Use wide_replace_strategy first.");
  }

  @Test
  void edit_rejectsPassageThatDoesNotMatchExactly() {
    current("Demo objective");

    assertThatThrownBy(() -> service.edit("demo objective", randomString()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("oldText was not found. Read strategy and supply an exact passage.");
  }

  @Test
  void edit_rejectsPassageWithOverlappingMatches() {
    current("aaaa");

    assertThatThrownBy(() -> service.edit("aaa", randomString()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("oldText matches more than once. Include more surrounding text.");
  }

  @Test
  void edit_rejectsResultThatWouldBeBlank() {
    var content = randomString();
    current(content);

    assertThatThrownBy(() -> service.edit(content, " \n"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Nonblank strategy content is required.");
    verify(repository, never()).save(any());
  }

  private UUID unset() {
    var id = UUID.randomUUID();
    when(repository.findFirstByOrderByIdAsc())
        .thenReturn(Optional.of(new Strategy(id, null, null)));
    return id;
  }

  private UUID current(String content) {
    var id = UUID.randomUUID();
    when(repository.findFirstByOrderByIdAsc())
        .thenReturn(Optional.of(new Strategy(id, content, Instant.now())));
    return id;
  }
}
