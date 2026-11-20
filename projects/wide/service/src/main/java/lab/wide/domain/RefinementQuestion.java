package lab.wide.domain;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;

public record RefinementQuestion(
    @Id UUID id,
    UUID problemId,
    UUID solutionId,
    String question,
    QuestionStatus status,
    String answer,
    String context,
    String source,
    Instant createdAt,
    Instant updatedAt) {}
