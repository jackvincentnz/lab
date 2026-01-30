package lab.mops.core.functional;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.HttpServletResponse;
import lab.mops.client.TestClient;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class BudgetFunctionalTest extends TestBase {

  @Autowired TestClient client;

  @Test
  void createBudget_createsBudget() {
    var name = randomString();

    var response = client.createBudget(name);

    assertThat(response.getSuccess()).isTrue();
    assertThat(response.getCode()).isEqualTo(HttpServletResponse.SC_CREATED);
    assertThat(response.getMessage()).isNotBlank();
    assertThat(response.getBudget().getId()).isNotBlank();
    assertThat(response.getBudget().getName()).isEqualTo(name);
    assertThat(response.getBudget().getCreatedAt()).isNotBlank();
    assertThat(response.getBudget().getUpdatedAt()).isNotBlank();
  }

  @Test
  void allBudgets_returnsAllBudgets() {
    var name = randomString();

    client.createBudget(name);

    var budgets = client.allBudgets();

    assertThat(budgets.size()).isEqualTo(2); // Default + added
    assertThat(budgets.get(1).getName()).isEqualTo(name);
  }
}
