package lab.autojournal.application;

import java.util.UUID;

public record TaskCompletedEvent(String taskId, UUID eventId) {}
