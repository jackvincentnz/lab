package lab.wide.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lab.test.TestBase;
import lab.wide.application.ProblemLink;
import lab.wide.application.RefinementService;
import lab.wide.application.SignalLink;
import lab.wide.domain.Problem;
import lab.wide.domain.ProblemSignal;
import lab.wide.domain.Solution;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefinementToolsTest extends TestBase {

  @Mock RefinementService service;

  @InjectMocks RefinementTools tools;

  @Captor ArgumentCaptor<List<SignalLink>> signalLinksCaptor;

  @Captor ArgumentCaptor<List<ProblemLink>> problemLinksCaptor;

  @Test
  void createProblem_acknowledgesOnlyTheSavedId() {
    var saved = problem();
    when(service.createProblem(any(), any(), any(), any(), any())).thenReturn(saved);

    var receipt = tools.createProblem(randomString(), randomString(), null, null, null);

    assertThat(receipt).isEqualTo(new WriteReceipt(saved.id()));
  }

  @Test
  void createSolution_passesConvertedSourceAndProblemLinks() {
    var signalLink = new SignalLinkInput(UUID.randomUUID(), randomString());
    var problemLink = new ProblemLinkInput(UUID.randomUUID(), randomString());
    when(service.createSolution(any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(solution());

    tools.createSolution(
        randomString(),
        randomString(),
        null,
        null,
        null,
        List.of(signalLink),
        List.of(problemLink));

    verify(service)
        .createSolution(
            any(),
            any(),
            any(),
            any(),
            any(),
            signalLinksCaptor.capture(),
            problemLinksCaptor.capture());
    assertThat(signalLinksCaptor.getValue())
        .containsExactly(new SignalLink(signalLink.signalId(), signalLink.rationale()));
    assertThat(problemLinksCaptor.getValue())
        .containsExactly(new ProblemLink(problemLink.problemId(), problemLink.rationale()));
  }

  @Test
  void unlinkProblemSignal_acknowledgesTheRemovedLinkId() {
    var link =
        new ProblemSignal(
            UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), randomString(), Instant.now());
    when(service.unlinkProblemSignal(eq(link.id()))).thenReturn(link);

    assertThat(tools.unlinkProblemSignal(link.id())).isEqualTo(new WriteReceipt(link.id()));
  }

  private Problem problem() {
    return new Problem(
        UUID.randomUUID(),
        randomString(),
        randomString(),
        null,
        null,
        Instant.now(),
        Instant.now());
  }

  private Solution solution() {
    return new Solution(
        UUID.randomUUID(),
        randomString(),
        randomString(),
        null,
        null,
        null,
        Instant.now(),
        Instant.now());
  }
}
