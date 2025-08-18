package lab.mops.core.domain.activity;

import java.time.Instant;

public record ActivityCreatedEvent(ActivityId activityId, String name, Instant createdAt) {}
