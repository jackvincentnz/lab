package lab.mops.core.domain.budget;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import lab.mops.core.domain.category.Category;
import lab.mops.core.domain.category.CategoryRepository;
import lab.test.RequiresDocker;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/** Covers what only Postgres shows: the migrations, column types, and child row keys. */
@SpringBootTest
@RequiresDocker
class LineItemRepositoryTest extends TestBase {

  static final PostgreSQLContainer POSTGRES =
      new PostgreSQLContainer(
          DockerImageName.parse(
                  "postgres:18.6@sha256:74935e72241653ca55e0414067e6d8763aceb8a810eb51b452253ec3dcfc4336")
              .withRepository("postgres"));

  static {
    POSTGRES.start();
  }

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
  }

  @Autowired BudgetRepository budgets;
  @Autowired CategoryRepository categories;
  @Autowired LineItemRepository lineItems;
  @Autowired JdbcTemplate jdbc;

  @Test
  void save_withChildCollections_roundTripsRows() {
    var lineItem = newLineItem();
    var category = Category.create(randomString());
    var firstValue = category.addValue(randomString());
    var secondValue = category.addValue(randomString());
    categories.save(category);
    var spend = randomSpend(randomDay());
    lineItem.planSpend(spend);
    lineItem.categorize(category, firstValue);
    lineItem.categorize(category, secondValue);

    lineItems.save(lineItem);

    var loaded = lineItems.findById(lineItem.getId()).orElseThrow();
    assertThat(loaded.getSpending()).containsExactly(spend);
    assertThat(loaded.getCategorizations())
        .containsExactlyInAnyOrder(
            Categorization.of(category.getId(), firstValue.getId()),
            Categorization.of(category.getId(), secondValue.getId()));
  }

  @Test
  void save_withChangedSpending_replacesRows() {
    var lineItem = newLineItem();
    var day = randomDay();
    var removed = randomSpend(day);
    var kept = randomSpend(day.plusDays(1));
    var added = randomSpend(day.plusDays(2));
    lineItem.planSpend(removed);
    lineItem.planSpend(kept);
    lineItems.save(lineItem);
    var loaded = lineItems.findById(lineItem.getId()).orElseThrow();
    loaded.getSpending().remove(removed);
    loaded.planSpend(added);

    lineItems.save(loaded);

    assertThat(lineItems.findById(lineItem.getId()).orElseThrow().getSpending())
        .containsExactlyInAnyOrder(kept, added);
    assertThat(spendRows(lineItem)).isEqualTo(2);
  }

  @Test
  void save_withDuplicateSpendDay_rollsBackAndKeepsRows() {
    var lineItem = newLineItem();
    var spend = randomSpend(randomDay());
    lineItem.planSpend(spend);
    lineItems.save(lineItem);
    // Bypass planSpend, which rejects a second spend on the same day before it reaches the table.
    lineItem
        .getSpending()
        .add(Spend.of(spend.getSpendDay(), spend.getAmount().add(BigDecimal.ONE)));

    assertThatThrownBy(() -> lineItems.save(lineItem)).isInstanceOf(DuplicateKeyException.class);

    assertThat(lineItems.findById(lineItem.getId()).orElseThrow().getSpending())
        .containsExactly(spend);
  }

  @Test
  void insertCategorization_withExistingValue_isRejected() {
    var lineItem = newLineItem();
    var category = Category.create(randomString());
    var value = category.addValue(randomString());
    categories.save(category);
    lineItem.categorize(category, value);
    lineItems.save(lineItem);

    assertThatThrownBy(
            () ->
                jdbc.update(
                    "INSERT INTO CATEGORIZATION (LINE_ITEM, CATEGORY_ID, CATEGORY_VALUE_ID)"
                        + " VALUES (?, ?, ?)",
                    lineItem.getId().toUUID(),
                    category.getId().toUUID(),
                    value.getId().toUUID()))
        .isInstanceOf(DuplicateKeyException.class);
  }

  private LineItem newLineItem() {
    var budget = budgets.save(Budget.create(randomString()));
    return lineItems.save(budget.addLineItem(randomString()));
  }

  private LocalDate randomDay() {
    return LocalDate.ofEpochDay(randomInt() % 36_500);
  }

  private Spend randomSpend(LocalDate day) {
    return Spend.of(day, BigDecimal.valueOf(randomInt(), 2));
  }

  private int spendRows(LineItem lineItem) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM SPEND WHERE LINE_ITEM = ?", Integer.class, lineItem.getId().toUUID());
  }
}
