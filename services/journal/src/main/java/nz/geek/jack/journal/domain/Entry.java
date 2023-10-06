package nz.geek.jack.journal.domain;

import java.time.Instant;

public final class Entry {

  private final EntryId id;

  private final String message;

  private final Instant createdAt;

  private Entry(EntryId id, String message, Instant createdAt) {
    this.id = id;
    this.message = message;
    this.createdAt = createdAt;
  }

  public EntryId getId() {
    return id;
  }

  public String getMessage() {
    return message;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public static Entry newEntry(String message) {
    return new Entry(EntryId.create(), message, Instant.now());
  }
}
