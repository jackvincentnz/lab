package lab.wide.domain;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;

public record RefinementDecision(
    @Id UUID id,
    UUID problemId,
    UUID solutionId,
    String decision,
    String rationale,
    String source,
    String revisitWhen,
    Instant createdAt,
    Instant updatedAt) {}
