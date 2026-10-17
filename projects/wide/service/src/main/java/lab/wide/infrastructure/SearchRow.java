package lab.wide.infrastructure;

import java.time.Instant;
import java.util.UUID;

/** Internal read projection; full text never leaves the search service. */
public record SearchRow(
    UUID id,
    String type,
    String title,
    Instant updatedAt,
    String content,
    String impact,
    String desiredOutcome,
    String scope,
    String tradeoffs,
    String effort,
    String source,
    int signalCount,
    int problemCount,
    int solutionCount,
    int openQuestionCount,
    int decisionCount) {}
