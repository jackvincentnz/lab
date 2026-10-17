package lab.wide.api;

import java.util.List;
import java.util.UUID;
import lab.wide.application.SearchService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class SearchTools {
  private final SearchService service;

  public SearchTools(SearchService service) {
    this.service = service;
  }

  @Tool(
      name = "wide_search",
      description =
          "Browse or search signals, problems and candidate solutions. Returns complete titles and"
              + " compact field previews, match snippets, relationship/question/decision counts and"
              + " hasMore; previews explicitly flag omitted text. Matches literal case-insensitive"
              + " text in entity fields, not question/decision bodies or link rationales. Newest"
              + " capture/update first, not ranked by value. Page through broad results before"
              + " narrowing; use wide_get_signal/problem/solution for full text and relationships"
              + " before making judgments or editing.")
  public SearchService.Page search(
      @ToolParam(required = false, description = "Text to find; omit or leave blank to browse")
          String query,
      @ToolParam(required = false, description = "SIGNAL, PROBLEM, SOLUTION; omit for all types")
          List<SearchService.Type> types,
      @ToolParam(
              required = false,
              description = "Only candidates linked to this problem; omit types or use SOLUTION")
          UUID problemId,
      @ToolParam(required = false, description = "Page size 1–50, default 20") Integer limit,
      @ToolParam(
              required = false,
              description = "Zero-based page, default 0; hasMore indicates further results")
          Integer page) {
    return service.search(query, types, problemId, limit, page);
  }
}
