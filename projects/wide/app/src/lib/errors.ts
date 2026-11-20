import { CombinedGraphQLErrors, type ErrorLike } from "@apollo/client";

/** The service rejected the request itself, such as a malformed id, so a retry cannot help. */
export function isBadRequest(error: ErrorLike | undefined) {
  return (
    CombinedGraphQLErrors.is(error) &&
    error.errors.every(
      (graphQLFormattedError) =>
        graphQLFormattedError.extensions?.["errorType"] === "BAD_REQUEST",
    )
  );
}
