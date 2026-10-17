package lab.autojournal.application;

import java.util.Optional;
import java.util.UUID;

/**
 * @param eventId identifies this event across redeliveries; empty when the producer published it
 *     before events carried an id.
 */
public record TaskCompletedEvent(String taskId, Optional<UUID> eventId) {}
