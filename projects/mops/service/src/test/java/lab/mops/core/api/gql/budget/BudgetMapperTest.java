package lab.mops.core.api.gql.budget;

import static org.assertj.core.api.Assertions.assertThat;

import lab.mops.core.domain.budget.Budget;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;

class BudgetMapperTest extends TestBase {

  BudgetMapper budgetMapper = new BudgetMapper();

  @Test
  void map_mapsId() {
    var domainBudget = Budget.create(randomString());

    var result = budgetMapper.map(domainBudget);

    assertThat(result.getId()).isEqualTo(domainBudget.getId().toString());
  }

  @Test
  void map_mapsName() {
    var name = randomString();
    var domainBudget = Budget.create(name);

    var result = budgetMapper.map(domainBudget);

    assertThat(result.getName()).isEqualTo(domainBudget.getName());
  }
}
