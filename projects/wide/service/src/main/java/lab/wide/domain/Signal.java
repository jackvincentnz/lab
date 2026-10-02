package lab.wide.domain;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;

public record Signal(
    @Id UUID id, String title, String content, String source, Instant capturedAt) {}
