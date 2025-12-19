package lab.mops.core.domain.budget;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public final class Spend {

  private final LocalDate spendDay;

  private final BigDecimal amount;

  private Spend(LocalDate spendDay, BigDecimal amount) {
    this.spendDay = spendDay;
    this.amount = amount;
  }

  public LocalDate getSpendDay() {
    return spendDay;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Spend spend = (Spend) o;
    return Objects.equals(spendDay, spend.spendDay) && Objects.equals(amount, spend.amount);
  }

  @Override
  public int hashCode() {
    return Objects.hash(spendDay, amount);
  }

  public static Spend of(LocalDate spendDay, BigDecimal amount) {
    return new Spend(spendDay, amount);
  }
}
