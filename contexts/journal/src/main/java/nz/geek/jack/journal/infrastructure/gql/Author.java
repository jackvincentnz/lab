package nz.geek.jack.journal.infrastructure.gql;

public final class Author {

  private final String id;

  private final String name;

  public Author(String id, String name) {
    this.id = id;
    this.name = name;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }
}
