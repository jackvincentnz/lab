package lab.mops.core.application.budget;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Month;
import java.util.Map;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

class BudgetPropertiesTest extends TestBase {

  @Test
  void bind_withNoProperties_startsFiscalYearInJanuary() {
    var binder = new Binder(new MapConfigurationPropertySource());

    var properties = binder.bindOrCreate("mops.budget", BudgetProperties.class);

    assertThat(properties.fiscalYearStartMonth()).isEqualTo(Month.JANUARY);
  }

  @Test
  void bind_withFiscalYearStartMonth_usesConfiguredMonth() {
    var binder =
        new Binder(
            new MapConfigurationPropertySource(
                Map.of("mops.budget.fiscal-year-start-month", "april")));

    var properties = binder.bindOrCreate("mops.budget", BudgetProperties.class);

    assertThat(properties.fiscalYearStartMonth()).isEqualTo(Month.APRIL);
  }
}
