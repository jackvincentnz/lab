package lab.wide.application;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import lab.wide.infrastructure.ProblemRepository;
import lab.wide.infrastructure.SearchRepository;
import lab.wide.infrastructure.SearchRow;
import org.springframework.stereotype.Service;

@Service
public class SearchService {
  public enum Type {
    SIGNAL,
    PROBLEM,
    SOLUTION
  }

  public record Preview(String text, boolean truncated) {}

  public record Match(String field, Preview preview) {}

  public record Result(
      Type type,
      UUID id,
      String title,
      Instant updatedAt,
      Map<String, Preview> previews,
      Match match,
      int signalCount,
      int problemCount,
      int solutionCount,
      int openQuestionCount,
      int decisionCount) {}

  public record Page(List<Result> results, int page, int limit, boolean hasMore) {}

  private final SearchRepository repository;
  private final ProblemRepository problems;

  public SearchService(SearchRepository repository, ProblemRepository problems) {
    this.repository = repository;
    this.problems = problems;
  }

  public Page search(String query, List<Type> types, UUID problemId, Integer limit, Integer page) {
    int size = limit == null ? 20 : limit;
    int number = page == null ? 0 : page;
    if (size < 1 || size > 50 || number < 0)
      throw new IllegalArgumentException("Limit must be 1–50 and page must be nonnegative.");
    var selected = types == null ? Set.of(Type.values()) : Set.copyOf(types);
    if (selected.isEmpty())
      throw new IllegalArgumentException("Types must not be empty; omit for all types.");
    if (problemId != null) {
      if (!problems.existsById(problemId))
        throw new IllegalArgumentException("Problem not found: " + problemId);
      if (types != null && !selected.equals(Set.of(Type.SOLUTION)))
        throw new IllegalArgumentException(
            "problemId selects linked candidates; omit types or use SOLUTION only.");
      selected = Set.of(Type.SOLUTION);
    }
    String text = query == null ? "" : query.strip();
    var rows =
        repository.search(
            text,
            selected.contains(Type.SIGNAL),
            selected.contains(Type.PROBLEM),
            selected.contains(Type.SOLUTION),
            problemId,
            size + 1,
            (long) number * size);
    var pattern =
        text.isEmpty()
            ? null
            : Pattern.compile(Pattern.quote(text), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    return new Page(
        rows.stream().limit(size).map(r -> compact(r, pattern)).toList(),
        number,
        size,
        rows.size() > size);
  }

  private static Preview preview(String value, int start, int length) {
    // Avoid splitting surrogate pairs at either boundary.
    int from = Math.min(start, value.length());
    if (from > 0 && from < value.length() && Character.isLowSurrogate(value.charAt(from))) from--;
    int to = Math.min(from + length, value.length());
    if (to < value.length() && to > from && Character.isHighSurrogate(value.charAt(to - 1))) to--;
    return new Preview(value.substring(from, to), from > 0 || to < value.length());
  }

  private static Result compact(SearchRow row, Pattern pattern) {
    var fields = new LinkedHashMap<String, String>();
    fields.put("title", row.title());
    fields.put(
        switch (row.type()) {
          case "SIGNAL" -> "content";
          case "PROBLEM" -> "description";
          default -> "approach";
        },
        row.content());
    fields.put("impact", row.impact());
    fields.put("desiredOutcome", row.desiredOutcome());
    fields.put("scope", row.scope());
    fields.put("tradeoffs", row.tradeoffs());
    fields.put("effort", row.effort());
    fields.put("source", row.source());
    var previews = new LinkedHashMap<String, Preview>();
    Match match = null;
    for (var field : fields.entrySet()) {
      if (field.getValue() == null) continue;
      if (!field.getKey().equals("title"))
        previews.put(
            field.getKey(),
            preview(
                field.getValue(),
                0,
                Set.of("content", "description", "approach").contains(field.getKey()) ? 240 : 120));
      if (pattern != null && match == null) {
        var found = pattern.matcher(field.getValue());
        if (found.find())
          match =
              new Match(
                  field.getKey(), preview(field.getValue(), Math.max(0, found.start() - 60), 240));
      }
    }
    return new Result(
        Type.valueOf(row.type()),
        row.id(),
        row.title(),
        row.updatedAt(),
        previews,
        match,
        row.signalCount(),
        row.problemCount(),
        row.solutionCount(),
        row.openQuestionCount(),
        row.decisionCount());
  }
}
