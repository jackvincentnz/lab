package lab.wide.application;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lab.wide.domain.Signal;
import lab.wide.infrastructure.SignalRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class SignalService {

  private final SignalRepository signalRepository;
  private final Clock clock;

  public SignalService(SignalRepository signalRepository, Clock clock) {
    this.signalRepository = signalRepository;
    this.clock = clock;
  }

  public Signal capture(String title, String content, String source) {
    if (title == null || title.isBlank()) {
      throw new IllegalArgumentException("Nonblank title is required.");
    }
    if (content == null || content.isBlank()) {
      throw new IllegalArgumentException("Nonblank content is required.");
    }

    String normalizedSource = source == null || source.isBlank() ? null : source;
    return signalRepository.save(
        new Signal(null, title, content, normalizedSource, Instant.now(clock)));
  }

  public Signal get(UUID id) {
    return find(id).orElseThrow(() -> new IllegalArgumentException("Signal not found: " + id));
  }

  public Optional<Signal> find(UUID id) {
    return signalRepository.findById(id);
  }

  public List<Signal> list(String query, Integer limit, Integer page) {
    int pageSize = limit == null ? 50 : limit;
    int pageNumber = page == null ? 0 : page;
    if (pageSize < 1 || pageSize > 100 || pageNumber < 0) {
      throw new IllegalArgumentException("Limit must be 1–100 and page must be nonnegative.");
    }

    var pageRequest =
        PageRequest.of(pageNumber, pageSize, Sort.by("capturedAt", "id").descending());
    return query == null || query.isEmpty()
        ? signalRepository.findAll(pageRequest).getContent()
        : signalRepository
            .findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrSourceContainingIgnoreCase(
                query, query, query, pageRequest);
  }
}
