package lab.wide.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import lab.test.TestBase;
import lab.wide.application.SearchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SearchToolsTest extends TestBase {

  @Mock SearchService service;

  @InjectMocks SearchTools tools;

  @Test
  void search_passesEveryArgumentThroughUnchanged() {
    var query = randomString();
    var types = List.of(SearchService.Type.SOLUTION);
    var problemId = UUID.randomUUID();
    var page = new SearchService.Page(List.of(), 2, 5, false);
    when(service.search(query, types, problemId, 5, 2)).thenReturn(page);

    assertThat(tools.search(query, types, problemId, 5, 2)).isSameAs(page);
  }
}
