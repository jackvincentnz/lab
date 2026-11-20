package lab.wide.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;
import lab.test.TestBase;
import lab.wide.domain.Strategy;
import lab.wide.domain.StrategyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StrategyServiceTest extends TestBase {

  @Mock StrategyRepository strategyRepository;

  final Clock clock = fixedClock();

  StrategyService strategyService;

  @BeforeEach
  void createService() {
    strategyService = new StrategyService(strategyRepository, clock);
  }

  @Test
  void read_seededRow_returnsIt() {
    var strategy = current(randomString());

    var read = strategyService.read();

    assertThat(read).isEqualTo(strategy);
  }

  @Test
  void read_missingRow_throwsIllegalState() {
    assertThatThrownBy(() -> strategyService.read())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Strategy row is missing. Check database migrations.");
  }

  @Test
  void replace_paddedContent_savesItOnTheExistingRow() {
    var strategy = unset();
    var content = "  " + randomString() + "\n" + randomString() + "  ";
    savesStrategy();

    var replaced = strategyService.replace(content);

    assertThat(replaced.id()).isEqualTo(strategy.id());
    assertThat(replaced.content()).isEqualTo(content);
    assertThat(replaced.updatedAt()).isEqualTo(clock.instant());
  }

  @Test
  void replace_blankContent_throwsWithoutSaving() {
    unset();

    assertThatThrownBy(() -> strategyService.replace(" \n\t"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Nonblank strategy content is required.");
    verify(strategyRepository, never()).save(any());
  }

  @Test
  void replace_nullContent_throwsWithoutSaving() {
    unset();

    assertThatThrownBy(() -> strategyService.replace(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Nonblank strategy content is required.");
    verify(strategyRepository, never()).save(any());
  }

  @Test
  void edit_singleOccurrence_replacesIt() {
    var prefix = randomString();
    var suffix = randomString();
    var newText = randomString();
    var strategy = current(prefix + " keep short " + suffix);
    savesStrategy();

    var edited = strategyService.edit("keep short", newText);

    assertThat(edited.id()).isEqualTo(strategy.id());
    assertThat(edited.content()).isEqualTo(prefix + " " + newText + " " + suffix);
    assertThat(edited.updatedAt()).isEqualTo(clock.instant());
  }

  @Test
  void edit_emptyNewText_removesThePassage() {
    var prefix = randomString();
    current(prefix + "\n- remove me");
    savesStrategy();

    var edited = strategyService.edit("\n- remove me", "");

    assertThat(edited.content()).isEqualTo(prefix);
  }

  @Test
  void edit_nullOldText_throwsBeforeReading() {
    assertThatThrownBy(() -> strategyService.edit(null, randomString()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Nonempty oldText is required.");
    verifyNoInteractions(strategyRepository);
  }

  @Test
  void edit_emptyOldText_throwsBeforeReading() {
    assertThatThrownBy(() -> strategyService.edit("", randomString()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Nonempty oldText is required.");
    verifyNoInteractions(strategyRepository);
  }

  @Test
  void edit_nullNewText_throwsBeforeReading() {
    assertThatThrownBy(() -> strategyService.edit(randomString(), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("newText is required; use an empty string to remove a passage.");
    verifyNoInteractions(strategyRepository);
  }

  @Test
  void edit_noStrategySet_throwsWithoutSaving() {
    unset();

    assertThatThrownBy(() -> strategyService.edit(randomString(), randomString()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("No strategy is set. Use wide_replace_strategy first.");
    verify(strategyRepository, never()).save(any());
  }

  @Test
  void edit_passageWithDifferentCase_throwsWithoutSaving() {
    current("Demo objective");

    assertThatThrownBy(() -> strategyService.edit("demo objective", randomString()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("oldText was not found. Read strategy and supply an exact passage.");
    verify(strategyRepository, never()).save(any());
  }

  @Test
  void edit_overlappingMatches_throwsWithoutSaving() {
    current("aaaa");

    assertThatThrownBy(() -> strategyService.edit("aaa", randomString()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("oldText matches more than once. Include more surrounding text.");
    verify(strategyRepository, never()).save(any());
  }

  @Test
  void edit_resultWouldBeBlank_throwsWithoutSaving() {
    var content = randomString();
    current(content);

    assertThatThrownBy(() -> strategyService.edit(content, " \n"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Nonblank strategy content is required.");
    verify(strategyRepository, never()).save(any());
  }

  private void savesStrategy() {
    when(strategyRepository.save(any(Strategy.class))).thenAnswer(returnsFirstArg());
  }

  private Strategy unset() {
    var strategy = new Strategy(UUID.randomUUID(), null, null);
    when(strategyRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(strategy));
    return strategy;
  }

  private Strategy current(String content) {
    var strategy = new Strategy(UUID.randomUUID(), content, clock.instant().minusSeconds(60));
    when(strategyRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(strategy));
    return strategy;
  }
}
