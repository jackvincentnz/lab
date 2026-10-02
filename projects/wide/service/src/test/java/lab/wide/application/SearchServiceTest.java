package lab.wide.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lab.test.TestBase;
import lab.wide.application.SearchService.Type;
import lab.wide.infrastructure.ProblemRepository;
import lab.wide.infrastructure.SearchRepository;
import lab.wide.infrastructure.SearchRow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest extends TestBase {

  @Mock SearchRepository repository;

  @Mock ProblemRepository problems;

  @InjectMocks SearchService service;

  @Test
  void search_browsesTwentyNewestByDefaultAndFetchesOneExtraRow() {
    var page = service.search(null, null, null, null, null);

    assertThat(page.page()).isZero();
    assertThat(page.limit()).isEqualTo(20);
    verify(repository).search("", true, true, true, null, 21, 0L);
  }

  @Test
  void search_offsetsByPageAndStripsTheQuery() {
    var query = randomString();

    service.search("  " + query + " ", null, null, 5, 2);

    verify(repository).search(query, true, true, true, null, 6, 10L);
  }

  @Test
  void search_selectsOnlyRequestedTypes() {
    service.search(null, List.of(Type.PROBLEM, Type.SOLUTION), null, null, null);

    verify(repository).search("", false, true, true, null, 21, 0L);
  }

  @Test
  void search_narrowsToCandidatesLinkedToTheProblem() {
    var problemId = UUID.randomUUID();
    when(problems.existsById(problemId)).thenReturn(true);

    service.search(null, null, problemId, null, null);

    verify(repository).search("", false, false, true, problemId, 21, 0L);
  }

  @Test
  void search_rejectsTypesOtherThanSolutionWithAProblemId() {
    var problemId = UUID.randomUUID();
    when(problems.existsById(problemId)).thenReturn(true);

    assertThatThrownBy(() -> service.search(null, List.of(Type.SIGNAL), problemId, null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("linked candidates");
    verifyNoInteractions(repository);
  }

  @Test
  void search_rejectsUnknownProblem() {
    var problemId = UUID.randomUUID();
    when(problems.existsById(problemId)).thenReturn(false);

    assertThatThrownBy(() -> service.search(null, null, problemId, null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Problem not found: " + problemId);
  }

  @Test
  void search_rejectsEmptyTypes() {
    assertThatThrownBy(() -> service.search(null, List.of(), null, null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("empty");
  }

  @Test
  void search_rejectsLimitOverFifty() {
    assertThatThrownBy(() -> service.search(null, null, null, 51, 0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Limit");
  }

  @Test
  void search_rejectsNegativePage() {
    assertThatThrownBy(() -> service.search(null, null, null, 1, -1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("page");
  }

  @Test
  void search_reportsHasMoreWhenAnExtraRowIsReturned() {
    var first = row("SIGNAL", randomString());
    stubRows(List.of(first, row("SIGNAL", randomString())));

    var page = service.search(null, null, null, 1, 0);

    assertThat(page.results()).extracting(SearchService.Result::id).containsExactly(first.id());
    assertThat(page.hasMore()).isTrue();
  }

  @Test
  void search_reportsNoMoreWhenThePageIsNotFull() {
    stubRows(List.of(row("SIGNAL", randomString())));

    var page = service.search(null, null, null, 1, 0);

    assertThat(page.hasMore()).isFalse();
  }

  @Test
  void search_previewsMainTextAtTwoHundredFortyAndOtherFieldsAtOneHundredTwenty() {
    var content = randomString(300);
    var source = randomString(200);
    stubRows(List.of(row("SIGNAL", content, source)));

    var result = service.search(null, null, null, null, null).results().get(0);

    var contentPreview = result.previews().get("content");
    assertThat(contentPreview.text()).isEqualTo(content.substring(0, 240));
    assertThat(contentPreview.truncated()).isTrue();
    var sourcePreview = result.previews().get("source");
    assertThat(sourcePreview.text()).isEqualTo(source.substring(0, 120));
    assertThat(sourcePreview.truncated()).isTrue();
  }

  @Test
  void search_keepsShortFieldsCompleteAndOmitsUnsetOnes() {
    var content = randomString();
    stubRows(List.of(row("SIGNAL", content, null)));

    var result = service.search(null, null, null, null, null).results().get(0);

    assertThat(result.previews()).containsOnlyKeys("content");
    assertThat(result.previews().get("content"))
        .isEqualTo(new SearchService.Preview(content, false));
  }

  @Test
  void search_namesTheMainFieldByType() {
    stubRows(
        List.of(
            row("SIGNAL", randomString()),
            row("PROBLEM", randomString()),
            row("SOLUTION", randomString())));

    var results = service.search(null, null, null, null, null).results();

    assertThat(results.get(0).previews()).containsOnlyKeys("content");
    assertThat(results.get(1).previews()).containsOnlyKeys("description");
    assertThat(results.get(2).previews()).containsOnlyKeys("approach");
  }

  @Test
  void search_snippetsTheFirstCaseInsensitiveMatchWithSurroundingText() {
    var needle = randomString();
    var content = "p".repeat(500) + needle + "s".repeat(500);
    stubRows(List.of(row("SIGNAL", content)));

    var result = service.search(needle.toUpperCase(), null, null, null, null).results().get(0);

    assertThat(result.match().field()).isEqualTo("content");
    assertThat(result.match().preview().text())
        .isEqualTo(content.substring(440, 680))
        .contains(needle);
    assertThat(result.match().preview().truncated()).isTrue();
  }

  @Test
  void search_prefersATitleMatchOverOtherFields() {
    var needle = randomString();
    var row =
        new SearchRow(
            UUID.randomUUID(),
            "PROBLEM",
            "Need " + needle,
            Instant.now(),
            "Also " + needle,
            null,
            null,
            null,
            null,
            null,
            null,
            0,
            0,
            0,
            0,
            0);
    stubRows(List.of(row));

    var result = service.search(needle, null, null, null, null).results().get(0);

    assertThat(result.match().field()).isEqualTo("title");
    assertThat(result.match().preview()).isEqualTo(new SearchService.Preview(row.title(), false));
  }

  @Test
  void search_leavesMatchUnsetWhenBrowsing() {
    stubRows(List.of(row("SIGNAL", randomString())));

    var result = service.search(null, null, null, null, null).results().get(0);

    assertThat(result.match()).isNull();
  }

  @Test
  void search_doesNotSplitSurrogatePairsAtThePreviewBoundary() {
    var content = "a" + "🧭".repeat(200);
    stubRows(List.of(row("SIGNAL", content)));

    var preview = service.search(null, null, null, null, null).results().get(0).previews();

    var text = preview.get("content").text();
    assertThat(text).hasSize(239);
    assertThat(Character.isHighSurrogate(text.charAt(text.length() - 1))).isFalse();
  }

  @Test
  void search_copiesRelationshipCounts() {
    var row =
        new SearchRow(
            UUID.randomUUID(),
            "PROBLEM",
            randomString(),
            Instant.now(),
            randomString(),
            null,
            null,
            null,
            null,
            null,
            null,
            1,
            2,
            3,
            4,
            5);
    stubRows(List.of(row));

    var result = service.search(null, null, null, null, null).results().get(0);

    assertThat(result.type()).isEqualTo(Type.PROBLEM);
    assertThat(result.title()).isEqualTo(row.title());
    assertThat(result.updatedAt()).isEqualTo(row.updatedAt());
    assertThat(result.signalCount()).isEqualTo(1);
    assertThat(result.problemCount()).isEqualTo(2);
    assertThat(result.solutionCount()).isEqualTo(3);
    assertThat(result.openQuestionCount()).isEqualTo(4);
    assertThat(result.decisionCount()).isEqualTo(5);
  }

  private void stubRows(List<SearchRow> rows) {
    when(repository.search(
            any(), anyBoolean(), anyBoolean(), anyBoolean(), any(), anyInt(), anyLong()))
        .thenReturn(rows);
  }

  private SearchRow row(String type, String content) {
    return row(type, content, null);
  }

  private SearchRow row(String type, String content, String source) {
    return new SearchRow(
        UUID.randomUUID(),
        type,
        randomString(),
        Instant.now(),
        content,
        null,
        null,
        null,
        null,
        null,
        source,
        0,
        0,
        0,
        0,
        0);
  }
}
