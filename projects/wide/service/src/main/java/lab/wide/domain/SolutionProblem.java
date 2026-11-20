package lab.wide.domain;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;

public record SolutionProblem(
    @Id UUID id, UUID solutionId, UUID problemId, String rationale, Instant linkedAt) {}
