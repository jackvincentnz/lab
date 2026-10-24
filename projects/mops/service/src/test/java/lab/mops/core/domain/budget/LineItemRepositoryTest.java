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

/** Covers what only Postgres shows: the migrations, column types, and table keys. */
@SpringBootTest
@RequiresDocker
class LineItemRepositoryTest extends TestBase {

  static final PostgreSQLContainer POSTGRES =
      new PostgreSQLContainer(
          DockerImageName.parse(
                  "postgres:18.6@sha256:edb0c08dc33befd0262ab3b5cf1c4f3f8cfd8b09e88c05b45adee29d6761ddb6")
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

  @Autowired BudgetRepository budgetRepository;
  @Autowired CategoryRepository categoryRepository;
  @Autowired LineItemRepository lineItemRepository;
  @Autowired JdbcTemplate jdbcTemplate;

  @Test
  void save_lineItemWithSpendingAndCategorizations_readsBackTheSameValues() {
    var channel = Category.create(randomString());
    var email = channel.addValue(randomString());
    var social = channel.addValue(randomString());

    categoryRepository.save(channel);

    var lineItem = newLineItem();
    var spend = randomSpend(randomDay());

    lineItem.planSpend(spend);
    lineItem.categorize(channel, email);
    lineItem.categorize(channel, social);

    lineItemRepository.save(lineItem);

    var storedLineItem = lineItemRepository.findById(lineItem.getId()).orElseThrow();
    assertThat(storedLineItem.getSpending()).containsExactly(spend);
    assertThat(storedLineItem.getCategorizations())
        .containsExactlyInAnyOrder(
            Categorization.of(channel.getId(), email.getId()),
            Categorization.of(channel.getId(), social.getId()));
  }

  @Test
  void save_afterAnotherSpendIsPlanned_storesEachSpendOnce() {
    var lineItem = newLineItem();
    var day = randomDay();
    var firstSpend = randomSpend(day);
    var secondSpend = randomSpend(day.plusDays(1));

    lineItem.planSpend(firstSpend);

    lineItemRepository.save(lineItem);

    var storedLineItem = lineItemRepository.findById(lineItem.getId()).orElseThrow();
    storedLineItem.planSpend(secondSpend);

    lineItemRepository.save(storedLineItem);

    assertThat(lineItemRepository.findById(lineItem.getId()).orElseThrow().getSpending())
        .containsExactlyInAnyOrder(firstSpend, secondSpend);
    assertThat(spendRowCount(lineItem)).isEqualTo(2);
  }

  @Test
  void insert_secondSpendOnTheSameDay_isRejectedByTheDatabase() {
    var lineItem = newLineItem();
    var spend = randomSpend(randomDay());
    lineItem.planSpend(spend);

    lineItemRepository.save(lineItem);

    // Insert directly, because planSpend rejects a second spend on the same day before saving.
    assertThatThrownBy(
            () ->
                jdbcTemplate.update(
                    "INSERT INTO SPEND (LINE_ITEM, SPEND_DAY, AMOUNT) VALUES (?, ?, ?)",
                    lineItem.getId().toUUID(),
                    spend.getSpendDay(),
                    spend.getAmount().add(BigDecimal.ONE)))
        .isInstanceOf(DuplicateKeyException.class);
  }

  @Test
  void insert_duplicateCategorization_isRejectedByTheDatabase() {
    var channel = Category.create(randomString());
    var email = channel.addValue(randomString());

    categoryRepository.save(channel);

    var lineItem = newLineItem();
    lineItem.categorize(channel, email);

    lineItemRepository.save(lineItem);

    // Insert directly, because the line item keeps its categorizations in a set.
    assertThatThrownBy(
            () ->
                jdbcTemplate.update(
                    "INSERT INTO CATEGORIZATION (LINE_ITEM, CATEGORY_ID, CATEGORY_VALUE_ID)"
                        + " VALUES (?, ?, ?)",
                    lineItem.getId().toUUID(),
                    channel.getId().toUUID(),
                    email.getId().toUUID()))
        .isInstanceOf(DuplicateKeyException.class);
  }

  private LineItem newLineItem() {
    var budget = budgetRepository.save(Budget.create(randomString()));
    return lineItemRepository.save(budget.addLineItem(randomString()));
  }

  private LocalDate randomDay() {
    return LocalDate.ofEpochDay(randomInt() % 36_500);
  }

  private Spend randomSpend(LocalDate day) {
    return Spend.of(day, BigDecimal.valueOf(randomInt(), 2));
  }

  private int spendRowCount(LineItem lineItem) {
    return jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM SPEND WHERE LINE_ITEM = ?", Integer.class, lineItem.getId().toUUID());
  }
}
