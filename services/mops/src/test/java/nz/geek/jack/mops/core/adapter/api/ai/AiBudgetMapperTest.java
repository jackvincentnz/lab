package nz.geek.jack.mops.core.adapter.api.ai;

import static org.assertj.core.api.Assertions.assertThat;

import nz.geek.jack.mops.core.domain.budget.Budget;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;

class AiBudgetMapperTest extends TestBase {

  AiBudgetMapper aiBudgetMapper = new AiBudgetMapper();

  @Test
  void map_mapsId() {
    var domainBudget = nz.geek.jack.mops.core.domain.budget.Budget.create(randomString());

    var result = aiBudgetMapper.map(domainBudget);

    assertThat(result.id()).isEqualTo(domainBudget.getId().toString());
  }

  @Test
  void map_mapsName() {
    var name = randomString();
    var domainBudget = Budget.create(name);

    var result = aiBudgetMapper.map(domainBudget);

    assertThat(result.name()).isEqualTo(domainBudget.getName());
  }
}
