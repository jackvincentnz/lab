package nz.geek.jack.mops.core.domain.category;

public record CategoryValueNameUpdatedEvent(
    CategoryId id, CategoryValueId categoryValueId, String name) {}
