package lab.wide.domain;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;

public record Strategy(@Id UUID id, String content, Instant updatedAt) {}
