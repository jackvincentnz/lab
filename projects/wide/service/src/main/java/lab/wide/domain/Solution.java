package lab.wide.domain;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;

public record Solution(
    @Id UUID id,
    String title,
    String approach,
    String scope,
    String tradeoffs,
    String effort,
    Instant createdAt,
    Instant updatedAt) {}
