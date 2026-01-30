package lab.libs.ddd.es.persistence;

import java.util.UUID;

public class ConcurrentStreamWriteException extends RuntimeException {
  public ConcurrentStreamWriteException(UUID streamId, int expectedVersion) {
    super(
        String.format(
            "Stream [%s] version doesn't match expected version [%s]", streamId, expectedVersion));
  }
}
