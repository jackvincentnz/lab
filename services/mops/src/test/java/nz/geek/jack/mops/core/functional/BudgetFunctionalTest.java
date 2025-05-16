package nz.geek.jack.mops.core.functional;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.HttpServletResponse;
import nz.geek.jack.test.TestBase;
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
  }
}
