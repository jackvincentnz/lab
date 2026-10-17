package lab.task.adapter.api.gql;

import com.netflix.graphql.types.errors.ErrorType;
import com.netflix.graphql.types.errors.TypedGraphQLError;
import graphql.GraphQLError;
import graphql.schema.DataFetchingEnvironment;
import lab.libs.ddd.domain.NotFoundException;
import lab.libs.ddd.domain.ValidationException;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;

// Maps domain exceptions to client error types for every data fetcher, so fetchers do not
// translate exceptions themselves. DGS reports any other exception as INTERNAL.
@ControllerAdvice
public class GraphqlExceptionAdvice {

  @GraphQlExceptionHandler
  public GraphQLError handle(NotFoundException e, DataFetchingEnvironment env) {
    return error(ErrorType.NOT_FOUND, e, env);
  }

  @GraphQlExceptionHandler
  public GraphQLError handle(ValidationException e, DataFetchingEnvironment env) {
    return error(ErrorType.BAD_REQUEST, e, env);
  }

  // Id parsing such as TaskId.fromString throws IllegalArgumentException for a malformed id.
  @GraphQlExceptionHandler
  public GraphQLError handle(IllegalArgumentException e, DataFetchingEnvironment env) {
    return error(ErrorType.BAD_REQUEST, e, env);
  }

  private static GraphQLError error(ErrorType errorType, Exception e, DataFetchingEnvironment env) {
    return TypedGraphQLError.newBuilder()
        .errorType(errorType)
        .message(e.getMessage())
        .path(env.getExecutionStepInfo().getPath())
        .build();
  }
}
