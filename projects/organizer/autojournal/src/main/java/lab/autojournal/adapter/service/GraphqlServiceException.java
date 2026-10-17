package lab.autojournal.adapter.service;

/** A downstream GraphQL service failed to answer a request with data. */
public class GraphqlServiceException extends RuntimeException {

  public GraphqlServiceException(String message) {
    super(message);
  }

  public GraphqlServiceException(String message, Throwable cause) {
    super(message, cause);
  }
}
