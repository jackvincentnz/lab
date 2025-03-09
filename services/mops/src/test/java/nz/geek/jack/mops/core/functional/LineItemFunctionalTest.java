package nz.geek.jack.mops.core.functional;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.TypeRef;
import com.netflix.graphql.dgs.DgsQueryExecutor;
import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import nz.geek.jack.mops.api.gql.client.AllLineItemsGraphQLQuery;
import nz.geek.jack.mops.api.gql.client.AllLineItemsProjectionRoot;
import nz.geek.jack.mops.api.gql.types.CategorizationInput;
import nz.geek.jack.mops.api.gql.types.LineItem;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class LineItemFunctionalTest extends TestBase {

  @Autowired DgsQueryExecutor dgsQueryExecutor;

  @Autowired TestClient client;

  @Test
  void addLineItemWithAllFields() {
    var name = randomString();
    var categoryId = client.createCategory(randomString()).getCategory().getId();
    var categoryValueId =
        client.addCategoryValue(categoryId, randomString()).getCategoryValue().getId();

    var response =
        client.addLineItem(
            name,
            List.of(
                CategorizationInput.newBuilder()
                    .categoryId(categoryId)
                    .categoryValueId(categoryValueId)
                    .build()));

    assertThat(response.getSuccess()).isTrue();
    assertThat(response.getCode()).isEqualTo(HttpServletResponse.SC_CREATED);
    assertThat(response.getMessage()).isNotBlank();
    assertThat(response.getLineItem().getName()).isEqualTo(name);

    var categorization = response.getLineItem().getCategorizations().get(0);

    assertThat(categorization.getCategory().getId()).isEqualTo(categoryId);
    assertThat(categorization.getCategoryValue().getId()).isEqualTo(categoryValueId);
  }

  @Test
  void categorizeLineItemReturnsAllFields() {
    var lineItemId = client.addLineItem(randomString()).getLineItem().getId();
    var categoryId = client.createCategory(randomString()).getCategory().getId();
    var categoryValueId =
        client.addCategoryValue(categoryId, randomString()).getCategoryValue().getId();

    var response = client.categorizeLineItem(lineItemId, categoryId, categoryValueId);

    assertThat(response.getSuccess()).isTrue();
    assertThat(response.getCode()).isEqualTo(HttpServletResponse.SC_OK);
    assertThat(response.getMessage()).isNotBlank();
    assertThat(response.getLineItem()).isNotNull();
  }

  @Test
  void allLineItemsReturnsAllFields() {
    var lineItemId = client.addLineItem(randomString()).getLineItem().getId();
    var category = client.createCategory(randomString()).getCategory();
    var categoryValue =
        client.addCategoryValue(category.getId(), randomString()).getCategoryValue();
    client.categorizeLineItem(lineItemId, category.getId(), categoryValue.getId());

    var request =
        new GraphQLQueryRequest(
            AllLineItemsGraphQLQuery.newRequest().build(),
            new AllLineItemsProjectionRoot<>()
                .id()
                .name()
                .categorizations()
                .category()
                .id()
                .name()
                .parent()
                .categoryValue()
                .id()
                .name());

    var result =
        dgsQueryExecutor.executeAndExtractJsonPathAsObject(
            request.serialize(), "data.allLineItems[0]", LineItem.class);

    assertThat(result.getId()).isNotBlank();
    assertThat(result.getName()).isNotBlank();
    var categorization = result.getCategorizations().get(0);
    assertThat(categorization.getCategory().getId()).isEqualTo(category.getId());
    assertThat(categorization.getCategory().getName()).isEqualTo(category.getName());
    assertThat(categorization.getCategoryValue().getId()).isEqualTo(categoryValue.getId());
    assertThat(categorization.getCategoryValue().getName()).isEqualTo(categoryValue.getName());
  }

  @Test
  void allLineItemsReturnsMultipleItems() {
    var lineItem1 = client.addLineItem(randomString()).getLineItem().getId();
    var lineItem2 = client.addLineItem(randomString()).getLineItem().getId();
    var lineItem3 = client.addLineItem(randomString()).getLineItem().getId();

    var request =
        new GraphQLQueryRequest(
            AllLineItemsGraphQLQuery.newRequest().build(), new AllLineItemsProjectionRoot<>().id());

    var addedIds =
        dgsQueryExecutor.executeAndExtractJsonPathAsObject(
            request.serialize(), "data.allLineItems[*].id", new TypeRef<List<String>>() {});

    assertThat(addedIds).hasSize(3);
    addedIds.forEach(id -> assertThat(List.of(lineItem1, lineItem2, lineItem3)).contains(id));
  }
}
