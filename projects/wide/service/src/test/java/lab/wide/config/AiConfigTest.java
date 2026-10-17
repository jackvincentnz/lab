package lab.wide.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import lab.test.TestBase;
import lab.wide.api.RefinementDetailTools;
import lab.wide.api.RefinementTools;
import lab.wide.api.SearchTools;
import lab.wide.api.SignalTools;
import lab.wide.api.StrategyTools;
import lab.wide.application.RefinementDetailService;
import lab.wide.application.RefinementService;
import lab.wide.application.SearchService;
import lab.wide.application.SignalService;
import lab.wide.application.StrategyService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallback;

class AiConfigTest extends TestBase {

  @Test
  void wideToolCallbacks_registersEveryTool() {
    var provider =
        new AiConfig()
            .wideToolCallbacks(
                new SignalTools(mock(SignalService.class)),
                new StrategyTools(mock(StrategyService.class)),
                new RefinementTools(mock(RefinementService.class)),
                new RefinementDetailTools(mock(RefinementDetailService.class)),
                new SearchTools(mock(SearchService.class)));

    assertThat(Arrays.stream(provider.getToolCallbacks()).map(AiConfigTest::name))
        .containsExactlyInAnyOrder(
            "wide_capture_signal",
            "wide_get_signal",
            "wide_read_strategy",
            "wide_replace_strategy",
            "wide_edit_strategy",
            "wide_create_problem",
            "wide_edit_problem",
            "wide_get_problem",
            "wide_create_solution",
            "wide_edit_solution",
            "wide_get_solution",
            "wide_link_problem_signal",
            "wide_unlink_problem_signal",
            "wide_link_solution_signal",
            "wide_unlink_solution_signal",
            "wide_link_solution_problem",
            "wide_unlink_solution_problem",
            "wide_create_question",
            "wide_edit_question",
            "wide_create_decision",
            "wide_edit_decision",
            "wide_search");
  }

  private static String name(ToolCallback callback) {
    return callback.getToolDefinition().name();
  }
}
