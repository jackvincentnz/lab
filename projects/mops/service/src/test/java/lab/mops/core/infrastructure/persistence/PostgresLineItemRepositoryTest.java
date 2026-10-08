package lab.mops.core.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import lab.mops.core.domain.budget.Budget;
import lab.mops.core.domain.budget.BudgetRepository;
import lab.mops.core.domain.budget.Categorization;
import lab.mops.core.domain.budget.LineItem;
import lab.mops.core.domain.budget.LineItemRepository;
import lab.mops.core.domain.budget.Spend;
import lab.mops.core.domain.category.Category;
import lab.mops.core.domain.category.CategoryRepository;
import lab.test.RequiresDocker;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@RequiresDocker
class PostgresLineItemRepositoryTest extends TestBase {

  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>(
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
  void save_withChildCollections_roundTripsAndReplacesRows() {
    var budget = budgets.save(Budget.create(randomString()));
    var category = Category.create(randomString());
    var firstValue = category.addValue(randomString());
    var secondValue = category.addValue(randomString());
    categories.save(category);
    var firstSpend = Spend.of(LocalDate.of(2026, 1, 15), new BigDecimal("123.45"));
    var secondSpend = Spend.of(LocalDate.of(2026, 2, 15), new BigDecimal("67.89"));
    var lineItem = budget.addLineItem(randomString());
    lineItem.planSpend(firstSpend);
    lineItem.categorize(category, firstValue);
    lineItems.save(lineItem);

    var loaded = lineItems.findById(lineItem.getId()).orElseThrow();
    assertThat(loaded.getBudgetId()).isEqualTo(budget.getId());
    assertThat(loaded.getName()).isEqualTo(lineItem.getName());
    assertThat(loaded.getSpending()).containsExactly(firstSpend);
    loaded.planSpend(secondSpend);
    loaded.categorize(category, secondValue);
    loaded.categorize(category, firstValue);
    lineItems.save(loaded);
    lineItems.save(loaded);

    var updated = lineItems.findById(lineItem.getId()).orElseThrow();
    assertThat(updated.getSpending()).containsExactlyInAnyOrder(firstSpend, secondSpend);
    assertThat(updated.getCategorizations())
        .containsExactlyInAnyOrder(
            Categorization.of(category.getId(), firstValue.getId()),
            Categorization.of(category.getId(), secondValue.getId()));
    assertThat(updated.getVersion()).isEqualTo(loaded.getVersion());
    assertThat(lineItems.findByBudgetId(budget.getId()))
        .extracting(LineItem::getId)
        .containsExactly(lineItem.getId());
    assertThat(childCount("SPEND", lineItem)).isEqualTo(2);
    assertThat(childCount("CATEGORIZATION", lineItem)).isEqualTo(2);

    var other = budget.addLineItem(randomString());
    other.planSpend(firstSpend);
    other.categorize(category, firstValue);
    lineItems.save(other);
    assertThat(childCount("SPEND", other)).isEqualTo(1);
    assertThat(childCount("CATEGORIZATION", other)).isEqualTo(1);

    updated.getSpending().remove(firstSpend);
    updated.getCategorizations().remove(Categorization.of(category.getId(), firstValue.getId()));
    lineItems.save(updated);
    var reduced = lineItems.findById(updated.getId()).orElseThrow();
    assertThat(reduced.getSpending()).containsExactly(secondSpend);
    assertThat(reduced.getCategorizations())
        .containsExactly(Categorization.of(category.getId(), secondValue.getId()));
    assertThat(childCount("SPEND", updated)).isEqualTo(1);
    assertThat(childCount("CATEGORIZATION", updated)).isEqualTo(1);
  }

  @Test
  void insertSpend_withSameLineItemAndDay_rejectsDifferentAmount() {
    var lineItem = newLineItem();
    var day = LocalDate.of(2026, 3, 15);
    lineItem.planSpend(Spend.of(day, new BigDecimal("12.34")));
    lineItems.save(lineItem);

    assertThatThrownBy(
            () ->
                jdbc.update(
                    "INSERT INTO SPEND (LINE_ITEM, SPEND_DAY, AMOUNT) VALUES (?, ?, ?)",
                    lineItem.getId().toUUID(),
                    day,
                    new BigDecimal("56.78")))
        .isInstanceOf(DuplicateKeyException.class);
    assertThat(childCount("SPEND", lineItem)).isEqualTo(1);
  }

  @Test
  void insertCategorization_withSameIdentity_rejectsDuplicate() {
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
    assertThat(childCount("CATEGORIZATION", lineItem)).isEqualTo(1);
  }

  @Test
  void save_withDuplicateSpend_rollsBackChildReplacement() {
    var lineItem = newLineItem();
    var category = Category.create(randomString());
    var value = category.addValue(randomString());
    categories.save(category);
    lineItem.categorize(category, value);
    var day = LocalDate.of(2026, 4, 15);
    var spend = Spend.of(day, new BigDecimal("12.34"));
    lineItem.planSpend(spend);
    lineItems.save(lineItem);
    var version = lineItem.getVersion();
    // Bypass planSpend to prove the database protects the day identity too.
    lineItem.getSpending().add(Spend.of(day, new BigDecimal("56.78")));

    assertThatThrownBy(() -> lineItems.save(lineItem)).isInstanceOf(DuplicateKeyException.class);

    var loaded = lineItems.findById(lineItem.getId()).orElseThrow();
    assertThat(loaded.getSpending()).containsExactly(spend);
    assertThat(loaded.getCategorizations())
        .containsExactly(Categorization.of(category.getId(), value.getId()));
    assertThat(loaded.getVersion()).isEqualTo(version);
    assertThat(childCount("SPEND", lineItem)).isEqualTo(1);
    assertThat(childCount("CATEGORIZATION", lineItem)).isEqualTo(1);
  }

  @Test
  void save_withStaleVersion_preservesCurrentSpending() {
    var lineItem = newLineItem();
    var stale = lineItems.findById(lineItem.getId()).orElseThrow();
    var spend = Spend.of(LocalDate.of(2026, 5, 15), new BigDecimal("12.34"));
    lineItem.planSpend(spend);
    lineItems.save(lineItem);
    stale.planSpend(Spend.of(LocalDate.of(2026, 6, 15), new BigDecimal("56.78")));

    assertThatThrownBy(() -> lineItems.save(stale))
        .isInstanceOf(OptimisticLockingFailureException.class);
    assertThat(lineItems.findById(lineItem.getId()).orElseThrow().getSpending())
        .containsExactly(spend);
  }

  @Test
  void delete_withChildCollections_removesOwnedRows() {
    var lineItem = newLineItem();
    var category = Category.create(randomString());
    var value = category.addValue(randomString());
    categories.save(category);
    lineItem.categorize(category, value);
    lineItem.planSpend(Spend.of(LocalDate.of(2026, 7, 15), new BigDecimal("12.34")));
    lineItems.save(lineItem);
    assertThat(childCount("SPEND", lineItem)).isEqualTo(1);
    assertThat(childCount("CATEGORIZATION", lineItem)).isEqualTo(1);

    lineItems.deleteById(lineItem.getId());

    assertThat(lineItems.findById(lineItem.getId())).isEmpty();
    assertThat(childCount("SPEND", lineItem)).isZero();
    assertThat(childCount("CATEGORIZATION", lineItem)).isZero();
    assertThat(budgets.findById(lineItem.getBudgetId())).isPresent();
    assertThat(categories.findById(category.getId())).isPresent();
  }

  private LineItem newLineItem() {
    var budget = budgets.save(Budget.create(randomString()));
    return lineItems.save(budget.addLineItem(randomString()));
  }

  private int childCount(String table, LineItem lineItem) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM " + table + " WHERE LINE_ITEM = ?",
        Integer.class,
        lineItem.getId().toUUID());
  }
}
