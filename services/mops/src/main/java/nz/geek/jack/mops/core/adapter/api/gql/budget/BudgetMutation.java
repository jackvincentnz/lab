package nz.geek.jack.mops.core.adapter.api.gql.budget;

import static jakarta.servlet.http.HttpServletResponse.SC_CREATED;
import static nz.geek.jack.mops.core.adapter.api.gql.ResponseMessage.CREATED_MESSAGE;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import nz.geek.jack.mops.api.gql.types.CreateBudgetInput;
import nz.geek.jack.mops.api.gql.types.CreateBudgetResponse;
import nz.geek.jack.mops.core.application.budget.BudgetCommandService;
import nz.geek.jack.mops.core.application.budget.CreateBudgetCommand;

@DgsComponent
public class BudgetMutation {

  private final BudgetCommandService budgetCommandService;

  private final BudgetMapper budgetMapper;

  public BudgetMutation(BudgetCommandService budgetCommandService, BudgetMapper budgetMapper) {
    this.budgetCommandService = budgetCommandService;
    this.budgetMapper = budgetMapper;
  }

  @DgsMutation
  public CreateBudgetResponse createBudget(@InputArgument("input") CreateBudgetInput input) {
    var command = new CreateBudgetCommand(input.getName());

    var budget = budgetCommandService.create(command);

    return CreateBudgetResponse.newBuilder()
        .code(SC_CREATED)
        .success(true)
        .message(CREATED_MESSAGE)
        .budget(budgetMapper.map(budget))
        .build();
  }
}
