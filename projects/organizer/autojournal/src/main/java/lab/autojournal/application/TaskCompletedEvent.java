package lab.autojournal.application;

import java.util.Optional;
import java.util.UUID;

/**
 * @param eventId identifies this event across redeliveries; empty when the message carried no valid
 *     id, such as one published before messages carried ids.
 */
public record TaskCompletedEvent(String taskId, Optional<UUID> eventId) {}
