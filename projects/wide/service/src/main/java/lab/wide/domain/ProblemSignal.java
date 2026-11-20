package lab.wide.domain;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;

public record ProblemSignal(
    @Id UUID id, UUID problemId, UUID signalId, String rationale, Instant linkedAt) {}
