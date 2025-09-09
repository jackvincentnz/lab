package lab.mops.client;

import com.jayway.jsonpath.TypeRef;
import com.netflix.graphql.dgs.DgsQueryExecutor;
import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest;
import java.util.List;
import lab.mops.api.gql.client.AddCategoryValueGraphQLQuery;
import lab.mops.api.gql.client.AddCategoryValueProjectionRoot;
import lab.mops.api.gql.client.AddLineItemGraphQLQuery;
import lab.mops.api.gql.client.AddLineItemProjectionRoot;
import lab.mops.api.gql.client.AllActivitiesGraphQLQuery;
import lab.mops.api.gql.client.AllActivitiesProjectionRoot;
import lab.mops.api.gql.client.AllBudgetsGraphQLQuery;
import lab.mops.api.gql.client.AllBudgetsProjectionRoot;
import lab.mops.api.gql.client.CategorizeLineItemGraphQLQuery;
import lab.mops.api.gql.client.CategorizeLineItemProjectionRoot;
import lab.mops.api.gql.client.ChatGraphQLQuery;
import lab.mops.api.gql.client.ChatProjectionRoot;
import lab.mops.api.gql.client.CreateActivityGraphQLQuery;
import lab.mops.api.gql.client.CreateActivityProjectionRoot;
import lab.mops.api.gql.client.CreateBudgetGraphQLQuery;
import lab.mops.api.gql.client.CreateBudgetProjectionRoot;
import lab.mops.api.gql.client.CreateCategoryGraphQLQuery;
import lab.mops.api.gql.client.CreateCategoryProjectionRoot;
import lab.mops.api.gql.client.DeleteAllLineItemsGraphQLQuery;
import lab.mops.api.gql.client.DeleteAllLineItemsProjectionRoot;
import lab.mops.api.gql.client.PlanSpendGraphQLQuery;
import lab.mops.api.gql.client.PlanSpendProjectionRoot;
import lab.mops.api.gql.client.StartChatGraphQLQuery;
import lab.mops.api.gql.client.StartChatProjectionRoot;
import lab.mops.api.gql.client.UpdateCategoryNameGraphQLQuery;
import lab.mops.api.gql.client.UpdateCategoryNameProjectionRoot;
import lab.mops.api.gql.client.UpdateCategoryValueNameGraphQLQuery;
import lab.mops.api.gql.client.UpdateCategoryValueNameProjectionRoot;
import lab.mops.api.gql.types.Activity;
import lab.mops.api.gql.types.AddCategoryValueInput;
import lab.mops.api.gql.types.AddCategoryValueResponse;
import lab.mops.api.gql.types.AddLineItemInput;
import lab.mops.api.gql.types.AddLineItemResponse;
import lab.mops.api.gql.types.Budget;
import lab.mops.api.gql.types.CategorizationInput;
import lab.mops.api.gql.types.CategorizeLineItemInput;
import lab.mops.api.gql.types.CategorizeLineItemResponse;
import lab.mops.api.gql.types.Chat;
import lab.mops.api.gql.types.CreateActivityInput;
import lab.mops.api.gql.types.CreateActivityResponse;
import lab.mops.api.gql.types.CreateBudgetInput;
import lab.mops.api.gql.types.CreateBudgetResponse;
import lab.mops.api.gql.types.CreateCategoryInput;
import lab.mops.api.gql.types.CreateCategoryResponse;
import lab.mops.api.gql.types.DeleteAllLineItemsResponse;
import lab.mops.api.gql.types.PlanSpendInput;
import lab.mops.api.gql.types.PlanSpendResponse;
import lab.mops.api.gql.types.SpendInput;
import lab.mops.api.gql.types.StartChatInput;
import lab.mops.api.gql.types.StartChatResponse;
import lab.mops.api.gql.types.UpdateCategoryNameInput;
import lab.mops.api.gql.types.UpdateCategoryNameResponse;
import lab.mops.api.gql.types.UpdateCategoryValueNameInput;
import lab.mops.api.gql.types.UpdateCategoryValueNameResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestClient {
  @Autowired DgsQueryExecutor dgsQueryExecutor;

  public CreateBudgetResponse createBudget(String name) {
    var request =
        new GraphQLQueryRequest(
            CreateBudgetGraphQLQuery.newRequest()
                .input(CreateBudgetInput.newBuilder().name(name).build())
                .build(),
            new CreateBudgetProjectionRoot<>()
                .success()
                .code()
                .message()
                .budget()
                .id()
                .name()
                .createdAt()
                .updatedAt());

    return dgsQueryExecutor.executeAndExtractJsonPathAsObject(
        request.serialize(), "data.createBudget", CreateBudgetResponse.class);
  }

  public List<Budget> allBudgets() {
    var request =
        new GraphQLQueryRequest(
            AllBudgetsGraphQLQuery.newRequest().build(),
            new AllBudgetsProjectionRoot<>().id().name());

    return dgsQueryExecutor.executeAndExtractJsonPathAsObject(
        request.serialize(), "data.allBudgets", new TypeRef<>() {});
  }

  public AddLineItemResponse addLineItem(String budgetId, String name) {
    return addLineItem(budgetId, name, List.of());
  }

  public AddLineItemResponse addLineItem(
      String budgetId, String name, List<CategorizationInput> categorizations) {
    var request =
        new GraphQLQueryRequest(
            AddLineItemGraphQLQuery.newRequest()
                .input(
                    AddLineItemInput.newBuilder()
                        .budgetId(budgetId)
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
                .budgetId()
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
                .amount()
                .parent()
                .spendTotals()
                .monthlyTotals()
                .total()
                .year()
                .month()
                .parent()
                .parent()
                .quarterlyTotals()
                .total()
                .fiscalYear()
                .quarter()
                .parent()
                .parent()
                .annualTotals()
                .total()
                .year()
                .parent()
                .grandTotal());

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

  public CreateActivityResponse createActivity(String name) {
    var request =
        new GraphQLQueryRequest(
            CreateActivityGraphQLQuery.newRequest()
                .input(CreateActivityInput.newBuilder().name(name).build())
                .build(),
            new CreateActivityProjectionRoot<>()
                .success()
                .code()
                .message()
                .activity()
                .id()
                .name()
                .createdAt()
                .updatedAt());

    return dgsQueryExecutor.executeAndExtractJsonPathAsObject(
        request.serialize(), "data.createActivity", CreateActivityResponse.class);
  }

  public List<Activity> allActivities() {
    var request =
        new GraphQLQueryRequest(
            AllActivitiesGraphQLQuery.newRequest().build(),
            new AllActivitiesProjectionRoot<>().id().name().createdAt().updatedAt());

    return dgsQueryExecutor.executeAndExtractJsonPathAsObject(
        request.serialize(), "data.allActivities", new TypeRef<>() {});
  }

  public StartChatResponse startChat(String userPrompt) {
    var request =
        new GraphQLQueryRequest(
            StartChatGraphQLQuery.newRequest()
                .input(StartChatInput.newBuilder().userPrompt(userPrompt).build())
                .build(),
            new StartChatProjectionRoot<>()
                .success()
                .code()
                .message()
                .chat()
                .createdAt()
                .updatedAt()
                .id()
                .messages()
                .id()
                .content()
                .createdAt()
                .updatedAt()
                .type()
                .parent()
                .status());

    return dgsQueryExecutor.executeAndExtractJsonPathAsObject(
        request.serialize(), "data.startChat", StartChatResponse.class);
  }

  public Chat chat(String chatId) {
    var request =
        new GraphQLQueryRequest(
            ChatGraphQLQuery.newRequest().id(chatId).build(),
            new ChatProjectionRoot<>()
                .id()
                .createdAt()
                .updatedAt()
                .messages()
                .id()
                .content()
                .createdAt()
                .updatedAt()
                .type()
                .parent()
                .status());

    return dgsQueryExecutor.executeAndExtractJsonPathAsObject(
        request.serialize(), "data.chat", Chat.class);
  }
}
