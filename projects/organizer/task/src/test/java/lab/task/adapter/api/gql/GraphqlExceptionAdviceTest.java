package lab.task.adapter.api.gql;

import static org.assertj.core.api.Assertions.assertThat;

import com.netflix.graphql.types.errors.ErrorType;
import graphql.GraphQLError;
import graphql.Scalars;
import graphql.execution.ExecutionStepInfo;
import graphql.execution.ResultPath;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingEnvironmentImpl;
import lab.libs.ddd.domain.NotFoundException;
import lab.libs.ddd.domain.ValidationException;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;

class GraphqlExceptionAdviceTest extends TestBase {

  private final GraphqlExceptionAdvice advice = new GraphqlExceptionAdvice();

  @Test
  void handle_notFoundException_returnsNotFoundError() {
    var path = ResultPath.rootPath().segment(randomString());
    var exception = new NotFoundException(randomId());

    var error = advice.handle(exception, environment(path));

    assertError(error, ErrorType.NOT_FOUND, exception.getMessage(), path);
  }

  @Test
  void handle_validationException_returnsBadRequestError() {
    var path = ResultPath.rootPath().segment(randomString());
    var exception = new ValidationException(randomString());

    var error = advice.handle(exception, environment(path));

    assertError(error, ErrorType.BAD_REQUEST, exception.getMessage(), path);
  }

  @Test
  void handle_illegalArgumentException_returnsBadRequestError() {
    var path = ResultPath.rootPath().segment(randomString());
    var exception = new IllegalArgumentException(randomString());

    var error = advice.handle(exception, environment(path));

    assertError(error, ErrorType.BAD_REQUEST, exception.getMessage(), path);
  }

  private static DataFetchingEnvironment environment(ResultPath path) {
    var stepInfo =
        ExecutionStepInfo.newExecutionStepInfo().type(Scalars.GraphQLString).path(path).build();
    return DataFetchingEnvironmentImpl.newDataFetchingEnvironment()
        .executionStepInfo(stepInfo)
        .build();
  }

  private static void assertError(
      GraphQLError error, ErrorType errorType, String message, ResultPath path) {
    assertThat(error.getMessage()).isEqualTo(message);
    assertThat(error.getPath()).isEqualTo(path.toList());
    assertThat(error.getExtensions()).containsEntry("errorType", errorType.name());
  }
}
