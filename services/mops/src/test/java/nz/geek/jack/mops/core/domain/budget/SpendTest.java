package nz.geek.jack.mops.core.domain.budget;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;

class SpendTest extends TestBase {

  @Test
  void of_setsSpendDay() {
    var spendDay = LocalDate.now();

    var result = Spend.of(spendDay, BigDecimal.valueOf(100));

    assertThat(result.getSpendDay()).isEqualTo(spendDay);
  }

  @Test
  void of_setAmount() {
    var amount = BigDecimal.valueOf(100);

    var result = Spend.of(LocalDate.now(), amount);

    assertThat(result.getAmount()).isEqualTo(amount);
  }

  @Test
  void equals_nullIsFalse() {
    var spend = Spend.of(LocalDate.now(), BigDecimal.valueOf(100));

    assertThat(spend.equals(null)).isFalse();
  }

  @Test
  void equals_otherObjectIsFalse() {
    var spend = Spend.of(LocalDate.now(), BigDecimal.valueOf(100));

    assertThat(spend.equals(new Object())).isFalse();
  }

  @Test
  void equals_sameDayAndAmountIsTrue() {
    var spendDay = LocalDate.now();
    var amount = BigDecimal.valueOf(100);

    var spend1 = Spend.of(spendDay, amount);
    var spend2 = Spend.of(spendDay, amount);

    assertThat(spend1.equals(spend2)).isTrue();
  }

  @Test
  void hashCode_sameDayAndAmountIsEqual() {
    var spendDay = LocalDate.now();
    var amount = BigDecimal.valueOf(100);

    var spend1 = Spend.of(spendDay, amount);
    var spend2 = Spend.of(spendDay, amount);

    assertThat(spend1.hashCode()).isEqualTo(spend2.hashCode());
  }
}
