package lab.mops.core.domain.time;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class QuarterTest {

  @Test
  void fromInt_validInput_returnsCorrectQuarter() {
    assertThat(Quarter.fromInt(1)).isEqualTo(Quarter.Q1);
    assertThat(Quarter.fromInt(2)).isEqualTo(Quarter.Q2);
    assertThat(Quarter.fromInt(3)).isEqualTo(Quarter.Q3);
    assertThat(Quarter.fromInt(4)).isEqualTo(Quarter.Q4);
  }

  @Test
  void fromInt_invalidInput_throwsException() {
    assertThatThrownBy(() -> Quarter.fromInt(0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Quarter must be between 1 and 4");

    assertThatThrownBy(() -> Quarter.fromInt(5))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Quarter must be between 1 and 4");
  }
}
