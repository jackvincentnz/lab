package nz.geek.jack.mops.core.functional;

import com.netflix.graphql.dgs.DgsQueryExecutor;
import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest;
import java.util.List;
import nz.geek.jack.mops.api.gql.client.AddCategoryValueGraphQLQuery;
import nz.geek.jack.mops.api.gql.client.AddCategoryValueProjectionRoot;
import nz.geek.jack.mops.api.gql.client.AddLineItemGraphQLQuery;
import nz.geek.jack.mops.api.gql.client.AddLineItemProjectionRoot;
import nz.geek.jack.mops.api.gql.client.CategorizeLineItemGraphQLQuery;
import nz.geek.jack.mops.api.gql.client.CategorizeLineItemProjectionRoot;
import nz.geek.jack.mops.api.gql.client.CreateCategoryGraphQLQuery;
import nz.geek.jack.mops.api.gql.client.CreateCategoryProjectionRoot;
import nz.geek.jack.mops.api.gql.client.DeleteAllLineItemsGraphQLQuery;
import nz.geek.jack.mops.api.gql.client.DeleteAllLineItemsProjectionRoot;
import nz.geek.jack.mops.api.gql.client.PlanSpendGraphQLQuery;
import nz.geek.jack.mops.api.gql.client.PlanSpendProjectionRoot;
import nz.geek.jack.mops.api.gql.client.UpdateCategoryNameGraphQLQuery;
import nz.geek.jack.mops.api.gql.client.UpdateCategoryNameProjectionRoot;
import nz.geek.jack.mops.api.gql.client.UpdateCategoryValueNameGraphQLQuery;
import nz.geek.jack.mops.api.gql.client.UpdateCategoryValueNameProjectionRoot;
import nz.geek.jack.mops.api.gql.types.AddCategoryValueInput;
import nz.geek.jack.mops.api.gql.types.AddCategoryValueResponse;
import nz.geek.jack.mops.api.gql.types.AddLineItemInput;
import nz.geek.jack.mops.api.gql.types.AddLineItemResponse;
import nz.geek.jack.mops.api.gql.types.CategorizationInput;
import nz.geek.jack.mops.api.gql.types.CategorizeLineItemInput;
import nz.geek.jack.mops.api.gql.types.CategorizeLineItemResponse;
import nz.geek.jack.mops.api.gql.types.CreateCategoryInput;
import nz.geek.jack.mops.api.gql.types.CreateCategoryResponse;
import nz.geek.jack.mops.api.gql.types.DeleteAllLineItemsResponse;
import nz.geek.jack.mops.api.gql.types.PlanSpendInput;
import nz.geek.jack.mops.api.gql.types.PlanSpendResponse;
import nz.geek.jack.mops.api.gql.types.SpendInput;
import nz.geek.jack.mops.api.gql.types.UpdateCategoryNameInput;
import nz.geek.jack.mops.api.gql.types.UpdateCategoryNameResponse;
import nz.geek.jack.mops.api.gql.types.UpdateCategoryValueNameInput;
import nz.geek.jack.mops.api.gql.types.UpdateCategoryValueNameResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestClient {
  @Autowired DgsQueryExecutor dgsQueryExecutor;

  public AddLineItemResponse addLineItem(String name) {
    return addLineItem(name, List.of());
  }

  public AddLineItemResponse addLineItem(String name, List<CategorizationInput> categorizations) {
    var request =
        new GraphQLQueryRequest(
            AddLineItemGraphQLQuery.newRequest()
                .input(
                    AddLineItemInput.newBuilder()
                        .name(name)
                        .categorizations(categorizations)
                        .build())
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

  public PlanSpendResponse planSpend(String lineItemId, SpendInput spendInput) {
    var request =
        new GraphQLQueryRequest(
            PlanSpendGraphQLQuery.newRequest()
                .input(
                    PlanSpendInput.newBuilder()
                        .lineItemId(lineItemId)
                        .spendInput(spendInput)
                        .build())
                .build(),
            new PlanSpendProjectionRoot<>()
                .success()
                .code()
                .message()
                .lineItem()
                .id()
                .spending()
                .day()
                .amount());

    return dgsQueryExecutor.executeAndExtractJsonPathAsObject(
        request.serialize(), "data.planSpend", PlanSpendResponse.class);
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

  public UpdateCategoryNameResponse updateCategoryName(String categoryId, String name) {
    var request =
        new GraphQLQueryRequest(
            UpdateCategoryNameGraphQLQuery.newRequest()
                .input(
                    UpdateCategoryNameInput.newBuilder().categoryId(categoryId).name(name).build())
                .build(),
            new UpdateCategoryNameProjectionRoot<>()
                .success()
                .code()
                .message()
                .category()
                .id()
                .name());

    return dgsQueryExecutor.executeAndExtractJsonPathAsObject(
        request.serialize(), "data.updateCategoryName", UpdateCategoryNameResponse.class);
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

  public UpdateCategoryValueNameResponse updateCategoryValueName(
      String categoryId, String categoryValueId, String name) {
    var request =
        new GraphQLQueryRequest(
            UpdateCategoryValueNameGraphQLQuery.newRequest()
                .input(
                    UpdateCategoryValueNameInput.newBuilder()
                        .categoryId(categoryId)
                        .categoryValueId(categoryValueId)
                        .name(name)
                        .build())
                .build(),
            new UpdateCategoryValueNameProjectionRoot<>()
                .success()
                .code()
                .message()
                .categoryValue()
                .id()
                .name());

    return dgsQueryExecutor.executeAndExtractJsonPathAsObject(
        request.serialize(), "data.updateCategoryValueName", UpdateCategoryValueNameResponse.class);
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

  public DeleteAllLineItemsResponse deleteAllLineItems() {
    var request =
        new GraphQLQueryRequest(
            DeleteAllLineItemsGraphQLQuery.newRequest().build(),
            new DeleteAllLineItemsProjectionRoot<>().success().code().message());

    return dgsQueryExecutor.executeAndExtractJsonPathAsObject(
        request.serialize(), "data.deleteAllLineItems", DeleteAllLineItemsResponse.class);
  }
}
