package lab.wide.application;

import java.time.Instant;
import lab.wide.domain.Strategy;
import lab.wide.infrastructure.StrategyRepository;
import org.springframework.stereotype.Service;

@Service
public class StrategyService {

  private final StrategyRepository repository;

  public StrategyService(StrategyRepository repository) {
    this.repository = repository;
  }

  public Strategy read() {
    return repository
        .findFirstByOrderByIdAsc()
        .orElseThrow(
            () -> new IllegalStateException("Strategy row is missing. Check database migrations."));
  }

  public Strategy replace(String content) {
    return save(read(), content);
  }

  public Strategy edit(String oldText, String newText) {
    if (oldText == null || oldText.isEmpty()) {
      throw new IllegalArgumentException("Nonempty oldText is required.");
    }
    if (newText == null) {
      throw new IllegalArgumentException(
          "newText is required; use an empty string to remove a passage.");
    }
    var strategy = read();
    var content = strategy.content();
    if (content == null) {
      throw new IllegalArgumentException("No strategy is set. Use wide_replace_strategy first.");
    }
    int start = content.indexOf(oldText);
    if (start < 0) {
      throw new IllegalArgumentException(
          "oldText was not found. Read strategy and supply an exact passage.");
    }
    if (content.indexOf(oldText, start + 1) >= 0) {
      throw new IllegalArgumentException(
          "oldText matches more than once. Include more surrounding text.");
    }
    return save(
        strategy,
        content.substring(0, start) + newText + content.substring(start + oldText.length()));
  }

  private Strategy save(Strategy strategy, String content) {
    if (content == null || content.isBlank()) {
      throw new IllegalArgumentException("Nonblank strategy content is required.");
    }
    return repository.save(new Strategy(strategy.id(), content, Instant.now()));
  }
}
