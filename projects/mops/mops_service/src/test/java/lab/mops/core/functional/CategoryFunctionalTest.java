package lab.mops.core.functional;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.TypeRef;
import com.netflix.graphql.dgs.DgsQueryExecutor;
import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import lab.mops.api.gql.client.AllCategoriesGraphQLQuery;
import lab.mops.api.gql.client.AllCategoriesProjectionRoot;
import lab.mops.api.gql.client.AllLineItemsProjectionRoot;
import lab.mops.api.gql.types.Category;
import lab.mops.client.TestClient;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class CategoryFunctionalTest extends TestBase {

  @Autowired DgsQueryExecutor dgsQueryExecutor;

  @Autowired TestClient client;

  @Test
  void createCategoryReturnsAllFields() {
    var name = randomString();

    var response = client.createCategory(name);

    assertThat(response.getSuccess()).isTrue();
    assertThat(response.getCode()).isEqualTo(HttpServletResponse.SC_CREATED);
    assertThat(response.getMessage()).isNotBlank();
    assertThat(response.getCategory()).isNotNull();
  }

  @Test
  void updateCategoryName_updatesName() {
    var name = randomString();
    var categoryId = client.createCategory(randomString()).getCategory().getId();

    var response = client.updateCategoryName(categoryId, name);

    assertThat(response.getSuccess()).isTrue();
    assertThat(response.getCode()).isEqualTo(HttpServletResponse.SC_OK);
    assertThat(response.getMessage()).isNotBlank();
    assertThat(response.getCategory().getName()).isEqualTo(name);
  }

  @Test
  void addCategoryValueReturnsAllFields() {
    var name = randomString();
    var categoryId = client.createCategory(randomString()).getCategory().getId();

    var response = client.addCategoryValue(categoryId, name);

    assertThat(response.getSuccess()).isTrue();
    assertThat(response.getCode()).isEqualTo(HttpServletResponse.SC_CREATED);
    assertThat(response.getMessage()).isNotBlank();
    assertThat(response.getCategoryValue()).isNotNull();
  }

  @Test
  void updateCategoryValueName_updatesName() {
    var name = randomString();
    var categoryId = client.createCategory(randomString()).getCategory().getId();
    var categoryValueId = client.addCategoryValue(categoryId, name).getCategoryValue().getId();

    var response = client.updateCategoryValueName(categoryId, categoryValueId, name);

    assertThat(response.getSuccess()).isTrue();
    assertThat(response.getCode()).isEqualTo(HttpServletResponse.SC_OK);
    assertThat(response.getMessage()).isNotBlank();
    assertThat(response.getCategoryValue().getName()).isEqualTo(name);
  }

  @Test
  void allCategoriesReturnsAllFields() {
    var categoryId = client.createCategory(randomString()).getCategory().getId();
    client.addCategoryValue(categoryId, randomString());

    var request =
        new GraphQLQueryRequest(
            AllCategoriesGraphQLQuery.newRequest().build(),
            new AllCategoriesProjectionRoot<>()
                .id()
                .name()
                .createdAt()
                .updatedAt()
                .values()
                .id()
                .name());

    var result =
        dgsQueryExecutor.executeAndExtractJsonPathAsObject(
            request.serialize(), "data.allCategories[0]", Category.class);

    assertThat(result.getId()).isNotBlank();
    assertThat(result.getName()).isNotBlank();
    assertThat(result.getCreatedAt()).isNotBlank();
    assertThat(result.getUpdatedAt()).isNotBlank();
    var value = result.getValues().get(0);
    assertThat(value.getId()).isNotBlank();
    assertThat(value.getName()).isNotBlank();
  }

  @Test
  void allCategoriesReturnsMultipleItems() {
    var category1 = client.createCategory(randomString()).getCategory().getId();
    var category2 = client.createCategory(randomString()).getCategory().getId();
    var category3 = client.createCategory(randomString()).getCategory().getId();

    var request =
        new GraphQLQueryRequest(
            AllCategoriesGraphQLQuery.newRequest().build(),
            new AllLineItemsProjectionRoot<>().id());

    var addedIds =
        dgsQueryExecutor.executeAndExtractJsonPathAsObject(
            request.serialize(), "data.allCategories[*].id", new TypeRef<List<String>>() {});

    assertThat(addedIds).hasSize(3);
    addedIds.forEach(id -> assertThat(List.of(category1, category2, category3)).contains(id));
  }
}
