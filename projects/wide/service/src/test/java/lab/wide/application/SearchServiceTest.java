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
import lab.wide.application.SearchService.Match;
import lab.wide.application.SearchService.Preview;
import lab.wide.application.SearchService.Result;
import lab.wide.application.SearchService.Type;
import lab.wide.domain.ProblemRepository;
import lab.wide.domain.SearchRepository;
import lab.wide.domain.SearchRow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest extends TestBase {

  @Mock SearchRepository searchRepository;

  @Mock ProblemRepository problemRepository;

  @InjectMocks SearchService searchService;

  @Test
  void search_noArguments_browsesTwentyNewestAndFetchesOneExtraRow() {
    var page = searchService.search(null, null, null, null, null);

    assertThat(page.page()).isZero();
    assertThat(page.limit()).isEqualTo(20);
    verify(searchRepository).search("", true, true, true, null, 21, 0L);
  }

  @Test
  void search_limitPageAndPaddedQuery_offsetsByPageAndStripsTheQuery() {
    var query = randomString();

    searchService.search("  " + query + " ", null, null, 5, 2);

    verify(searchRepository).search(query, true, true, true, null, 6, 10L);
  }

  @Test
  void search_selectedTypes_includesOnlyThoseTypes() {
    searchService.search(null, List.of(Type.PROBLEM, Type.SOLUTION), null, null, null);

    verify(searchRepository).search("", false, true, true, null, 21, 0L);
  }

  @Test
  void search_problemIdWithoutTypes_includesOnlyLinkedSolutions() {
    var problemId = existingProblemId();

    searchService.search(null, null, problemId, null, null);

    verify(searchRepository).search("", false, false, true, problemId, 21, 0L);
  }

  @Test
  void search_problemIdWithSolutionType_includesOnlyLinkedSolutions() {
    var problemId = existingProblemId();

    searchService.search(null, List.of(Type.SOLUTION), problemId, null, null);

    verify(searchRepository).search("", false, false, true, problemId, 21, 0L);
  }

  @Test
  void search_problemIdWithOtherTypes_throwsWithoutSearching() {
    var problemId = existingProblemId();
    var types = List.of(Type.SIGNAL, Type.SOLUTION);

    assertThatThrownBy(() -> searchService.search(null, types, problemId, null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("problemId selects linked candidates; omit types or use SOLUTION only.");
    verifyNoInteractions(searchRepository);
  }

  @Test
  void search_unknownProblemId_throwsWithoutSearching() {
    var problemId = UUID.randomUUID();

    assertThatThrownBy(() -> searchService.search(null, null, problemId, null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Problem not found: " + problemId);
    verifyNoInteractions(searchRepository);
  }

  @Test
  void search_emptyTypes_throwsWithoutSearching() {
    assertThatThrownBy(() -> searchService.search(null, List.of(), null, null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Types must not be empty; omit for all types.");
    verifyNoInteractions(searchRepository);
  }

  @Test
  void search_limitBelowOne_throwsWithoutSearching() {
    assertThatThrownBy(() -> searchService.search(null, null, null, 0, 0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Limit must be 1–50 and page must be nonnegative.");
    verifyNoInteractions(searchRepository);
  }

  @Test
  void search_limitOverFifty_throwsWithoutSearching() {
    assertThatThrownBy(() -> searchService.search(null, null, null, 51, 0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Limit must be 1–50 and page must be nonnegative.");
    verifyNoInteractions(searchRepository);
  }

  @Test
  void search_negativePage_throwsWithoutSearching() {
    assertThatThrownBy(() -> searchService.search(null, null, null, 1, -1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Limit must be 1–50 and page must be nonnegative.");
    verifyNoInteractions(searchRepository);
  }

  @Test
  void search_extraRowReturned_reportsMoreAndDropsTheExtraRow() {
    var first = searchRow("SIGNAL", randomString(), null);
    returns(first, searchRow("SIGNAL", randomString(), null));

    var page = searchService.search(null, null, null, 1, 0);

    assertThat(page.results()).extracting(Result::id).containsExactly(first.id());
    assertThat(page.hasMore()).isTrue();
  }

  @Test
  void search_pageNotFull_reportsNoMore() {
    returns(searchRow("SIGNAL", randomString(), null));

    var page = searchService.search(null, null, null, 1, 0);

    assertThat(page.hasMore()).isFalse();
  }

  @Test
  void search_longFields_previewsMainTextAt240AndOtherFieldsAt120() {
    var content = randomString(300);
    var source = randomString(200);
    returns(searchRow("SIGNAL", content, source));

    var result = searchService.search(null, null, null, null, null).results().get(0);

    assertThat(result.previews().get("content"))
        .isEqualTo(new Preview(content.substring(0, 240), true));
    assertThat(result.previews().get("source"))
        .isEqualTo(new Preview(source.substring(0, 120), true));
  }

  @Test
  void search_shortAndUnsetFields_keepsShortOnesCompleteAndOmitsUnsetOnes() {
    var content = randomString();
    returns(searchRow("SIGNAL", content, null));

    var result = searchService.search(null, null, null, null, null).results().get(0);

    assertThat(result.previews()).containsOnlyKeys("content");
    assertThat(result.previews().get("content")).isEqualTo(new Preview(content, false));
  }

  @Test
  void search_eachType_namesTheMainFieldForThatType() {
    returns(
        searchRow("SIGNAL", randomString(), null),
        searchRow("PROBLEM", randomString(), null),
        searchRow("SOLUTION", randomString(), null));

    var results = searchService.search(null, null, null, null, null).results();

    assertThat(results.get(0).previews()).containsOnlyKeys("content");
    assertThat(results.get(1).previews()).containsOnlyKeys("description");
    assertThat(results.get(2).previews()).containsOnlyKeys("approach");
  }

  @Test
  void search_solutionFields_previewsScopeTradeoffsAndEffort() {
    var searchRow =
        new SearchRow(
            UUID.randomUUID(),
            "SOLUTION",
            randomString(),
            Instant.now(),
            randomString(),
            null,
            null,
            randomString(),
            randomString(),
            randomString(),
            null,
            0,
            0,
            0,
            0,
            0);
    returns(searchRow);

    var result = searchService.search(null, null, null, null, null).results().get(0);

    assertThat(result.previews())
        .containsOnlyKeys("approach", "scope", "tradeoffs", "effort")
        .containsEntry("scope", new Preview(searchRow.scope(), false));
  }

  @Test
  void search_matchDeepInText_snippetsAroundTheFirstCaseInsensitiveMatch() {
    var needle = randomString();
    var content = "p".repeat(500) + needle + "s".repeat(500);
    returns(searchRow("SIGNAL", content, null));

    var result =
        searchService.search(needle.toUpperCase(), null, null, null, null).results().get(0);

    assertThat(result.match())
        .isEqualTo(new Match("content", new Preview(content.substring(440, 680), true)));
  }

  @Test
  void search_matchInTitleAndContent_prefersTheTitle() {
    var needle = randomString();
    var title = randomString() + needle;
    var searchRow =
        new SearchRow(
            UUID.randomUUID(),
            "PROBLEM",
            title,
            Instant.now(),
            needle + randomString(),
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
    returns(searchRow);

    var result = searchService.search(needle, null, null, null, null).results().get(0);

    assertThat(result.match()).isEqualTo(new Match("title", new Preview(title, false)));
  }

  @Test
  void search_queryMatchesNoField_leavesMatchUnset() {
    returns(searchRow("SIGNAL", randomString(), null));

    var result = searchService.search("~", null, null, null, null).results().get(0);

    assertThat(result.match()).isNull();
  }

  @Test
  void search_browsing_leavesMatchUnset() {
    returns(searchRow("SIGNAL", randomString(), null));

    var result = searchService.search(" ", null, null, null, null).results().get(0);

    assertThat(result.match()).isNull();
  }

  @Test
  void search_surrogatePairAtThePreviewEnd_keepsThePairWhole() {
    var content = "a" + "🧭".repeat(200);
    returns(searchRow("SIGNAL", content, null));

    var result = searchService.search(null, null, null, null, null).results().get(0);

    assertThat(result.previews().get("content").text()).isEqualTo(content.substring(0, 239));
  }

  @Test
  void search_surrogatePairAtTheSnippetStart_keepsThePairWhole() {
    var needle = randomString();
    var content = "a" + "🧭".repeat(40) + "b" + needle;
    returns(searchRow("SIGNAL", content, null));

    var result = searchService.search(needle, null, null, null, null).results().get(0);

    assertThat(result.match().preview()).isEqualTo(new Preview(content.substring(21), true));
  }

  @Test
  void search_resultRow_copiesIdentityAndRelationshipCounts() {
    var searchRow =
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
    returns(searchRow);

    var result = searchService.search(null, null, null, null, null).results().get(0);

    assertThat(result.type()).isEqualTo(Type.PROBLEM);
    assertThat(result.id()).isEqualTo(searchRow.id());
    assertThat(result.title()).isEqualTo(searchRow.title());
    assertThat(result.updatedAt()).isEqualTo(searchRow.updatedAt());
    assertThat(result.signalCount()).isEqualTo(1);
    assertThat(result.problemCount()).isEqualTo(2);
    assertThat(result.solutionCount()).isEqualTo(3);
    assertThat(result.openQuestionCount()).isEqualTo(4);
    assertThat(result.decisionCount()).isEqualTo(5);
  }

  private UUID existingProblemId() {
    var problemId = UUID.randomUUID();
    when(problemRepository.existsById(problemId)).thenReturn(true);
    return problemId;
  }

  private void returns(SearchRow... searchRows) {
    when(searchRepository.search(
            any(), anyBoolean(), anyBoolean(), anyBoolean(), any(), anyInt(), anyLong()))
        .thenReturn(List.of(searchRows));
  }

  private SearchRow searchRow(String type, String content, String source) {
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
