package lab.wide.domain;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;

public record Problem(
    @Id UUID id,
    String title,
    String description,
    String impact,
    String desiredOutcome,
    Instant createdAt,
    Instant updatedAt) {}
