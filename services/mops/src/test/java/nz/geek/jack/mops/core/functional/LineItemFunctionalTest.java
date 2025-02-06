package nz.geek.jack.mops.core.functional;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.TypeRef;
import com.netflix.graphql.dgs.DgsQueryExecutor;
import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import nz.geek.jack.mops.api.gql.client.AddLineItemGraphQLQuery;
import nz.geek.jack.mops.api.gql.client.AddLineItemProjectionRoot;
import nz.geek.jack.mops.api.gql.client.AllLineItemsGraphQLQuery;
import nz.geek.jack.mops.api.gql.client.AllLineItemsProjectionRoot;
import nz.geek.jack.mops.api.gql.types.AddLineItemInput;
import nz.geek.jack.mops.api.gql.types.AddLineItemResponse;
import nz.geek.jack.mops.api.gql.types.LineItem;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional // Roll back test state to avoid database interactions between tests
class LineItemFunctionalTest extends TestBase {

  @Autowired DgsQueryExecutor dgsQueryExecutor;

  @Test
  void addLineItemReturnsAllFields() {
    var name = randomString();
    var request =
        new GraphQLQueryRequest(
            AddLineItemGraphQLQuery.newRequest()
                .input(AddLineItemInput.newBuilder().name(name).build())
                .build(),
            new AddLineItemProjectionRoot<>().success().code().message().lineItem().id().name());

    var lineItem =
        dgsQueryExecutor.executeAndExtractJsonPathAsObject(
            request.serialize(), "data.addLineItem", AddLineItemResponse.class);

    assertThat(lineItem.getSuccess()).isTrue();
    assertThat(lineItem.getCode()).isEqualTo(HttpServletResponse.SC_CREATED);
    assertThat(lineItem.getMessage()).isNotBlank();
    assertThat(lineItem.getLineItem().getId()).isNotBlank();
    assertThat(lineItem.getLineItem().getName()).isEqualTo(name);
  }

  @Test
  void allLineItemsReturnsAllFields() {
    addLineItem();
    var request =
        new GraphQLQueryRequest(
            AllLineItemsGraphQLQuery.newRequest().build(),
            new AllLineItemsProjectionRoot<>().id().name());

    var result =
        dgsQueryExecutor.executeAndExtractJsonPathAsObject(
            request.serialize(), "data.allLineItems[0]", LineItem.class);

    assertThat(result.getId()).isNotBlank();
    assertThat(result.getName()).isNotBlank();
  }

  @Test
  void addLineItemReturnsMultipleItems() {
    var lineItem1 = addLineItem();
    var lineItem2 = addLineItem();
    var lineItem3 = addLineItem();
    var request =
        new GraphQLQueryRequest(
            AllLineItemsGraphQLQuery.newRequest().build(), new AllLineItemsProjectionRoot<>().id());

    var addedIds =
        dgsQueryExecutor.executeAndExtractJsonPathAsObject(
            request.serialize(), "data.allLineItems[*].id", new TypeRef<List<String>>() {});

    assertThat(addedIds).hasSize(3);
    addedIds.forEach(id -> assertThat(List.of(lineItem1, lineItem2, lineItem3)).contains(id));
  }

  private String addLineItem() {
    var request =
        new GraphQLQueryRequest(
            AddLineItemGraphQLQuery.newRequest()
                .input(AddLineItemInput.newBuilder().name(randomString()).build())
                .build(),
            new AddLineItemProjectionRoot<>().lineItem().id());

    return dgsQueryExecutor.executeAndExtractJsonPath(
        request.serialize(), "data.addLineItem.lineItem.id");
  }
}
