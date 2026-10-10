package lab.wide.domain;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;

public record SolutionSignal(
    @Id UUID id, UUID solutionId, UUID signalId, String rationale, Instant linkedAt) {}
