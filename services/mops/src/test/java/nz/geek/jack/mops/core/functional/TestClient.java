package nz.geek.jack.mops.core.functional;

import com.netflix.graphql.dgs.DgsQueryExecutor;
import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest;
import nz.geek.jack.mops.api.gql.client.AddCategoryValueGraphQLQuery;
import nz.geek.jack.mops.api.gql.client.AddCategoryValueProjectionRoot;
import nz.geek.jack.mops.api.gql.client.AddLineItemGraphQLQuery;
import nz.geek.jack.mops.api.gql.client.AddLineItemProjectionRoot;
import nz.geek.jack.mops.api.gql.client.CategorizeLineItemGraphQLQuery;
import nz.geek.jack.mops.api.gql.client.CategorizeLineItemProjectionRoot;
import nz.geek.jack.mops.api.gql.client.CreateCategoryGraphQLQuery;
import nz.geek.jack.mops.api.gql.client.CreateCategoryProjectionRoot;
import nz.geek.jack.mops.api.gql.types.AddCategoryValueInput;
import nz.geek.jack.mops.api.gql.types.AddCategoryValueResponse;
import nz.geek.jack.mops.api.gql.types.AddLineItemInput;
import nz.geek.jack.mops.api.gql.types.AddLineItemResponse;
import nz.geek.jack.mops.api.gql.types.CategorizeLineItemInput;
import nz.geek.jack.mops.api.gql.types.CategorizeLineItemResponse;
import nz.geek.jack.mops.api.gql.types.CreateCategoryInput;
import nz.geek.jack.mops.api.gql.types.CreateCategoryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestClient {
  @Autowired DgsQueryExecutor dgsQueryExecutor;

  public AddLineItemResponse addLineItem(String name) {
    var request =
        new GraphQLQueryRequest(
            AddLineItemGraphQLQuery.newRequest()
                .input(AddLineItemInput.newBuilder().name(name).build())
                .build(),
            new AddLineItemProjectionRoot<>()
                .success()
                .code()
                .message()
                .lineItem()
                .id()
                .name()
                .categorizations()
                .category()
                .id()
                .parent()
                .categoryValue()
                .id());

    return dgsQueryExecutor.executeAndExtractJsonPathAsObject(
        request.serialize(), "data.addLineItem", AddLineItemResponse.class);
  }

  public CreateCategoryResponse createCategory(String name) {
    var request =
        new GraphQLQueryRequest(
            CreateCategoryGraphQLQuery.newRequest()
                .input(CreateCategoryInput.newBuilder().name(name).build())
                .build(),
            new CreateCategoryProjectionRoot<>()
                .success()
                .code()
                .message()
                .category()
                .id()
                .name()
                .values()
                .id()
                .name());

    return dgsQueryExecutor.executeAndExtractJsonPathAsObject(
        request.serialize(), "data.createCategory", CreateCategoryResponse.class);
  }

  public AddCategoryValueResponse addCategoryValue(String categoryId, String name) {
    var request =
        new GraphQLQueryRequest(
            AddCategoryValueGraphQLQuery.newRequest()
                .input(AddCategoryValueInput.newBuilder().categoryId(categoryId).name(name).build())
                .build(),
            new AddCategoryValueProjectionRoot<>()
                .success()
                .code()
                .message()
                .categoryValue()
                .id()
                .name());

    return dgsQueryExecutor.executeAndExtractJsonPathAsObject(
        request.serialize(), "data.addCategoryValue", AddCategoryValueResponse.class);
  }

  public CategorizeLineItemResponse categorizeLineItem(
      String lineItemId, String categoryId, String categoryValueId) {
    var request =
        new GraphQLQueryRequest(
            CategorizeLineItemGraphQLQuery.newRequest()
                .input(
                    CategorizeLineItemInput.newBuilder()
                        .lineItemId(lineItemId)
                        .categoryId(categoryId)
                        .categoryValueId(categoryValueId)
                        .build())
                .build(),
            new CategorizeLineItemProjectionRoot<>()
                .success()
                .code()
                .message()
                .lineItem()
                .id()
                .name()
                .categorizations()
                .category()
                .id()
                .parent()
                .categoryValue()
                .id());

    return dgsQueryExecutor.executeAndExtractJsonPathAsObject(
        request.serialize(), "data.categorizeLineItem", CategorizeLineItemResponse.class);
  }
}
