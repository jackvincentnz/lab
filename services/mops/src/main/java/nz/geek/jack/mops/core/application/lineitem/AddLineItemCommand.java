package nz.geek.jack.mops.core.application.lineitem;

import java.util.Collection;
import java.util.Optional;
import nz.geek.jack.mops.core.domain.lineitem.Categorization;

public final class AddLineItemCommand {

  private final String name;

  private Collection<Categorization> categorizations;

  public AddLineItemCommand(String name) {
    this.name = name;
  }

  public AddLineItemCommand withCategorizations(Collection<Categorization> categorizations) {
    this.categorizations = categorizations;
    return this;
  }

  public String getName() {
    return name;
  }

  public Optional<Collection<Categorization>> getCategorizations() {
    return Optional.ofNullable(categorizations);
  }
}
