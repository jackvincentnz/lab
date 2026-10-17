package lab.wide.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;
import lab.test.TestBase;
import lab.wide.application.RefinementDetailService;
import lab.wide.domain.QuestionStatus;
import lab.wide.domain.RefinementQuestion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefinementDetailToolsTest extends TestBase {

  @Mock RefinementDetailService service;

  @InjectMocks RefinementDetailTools tools;

  @Test
  void createQuestion_acknowledgesOnlyTheSavedId() {
    var saved = question();
    when(service.createQuestion(any(), any(), any(), any(), any(), any(), any())).thenReturn(saved);

    var receipt =
        tools.createQuestion(saved.problemId(), null, saved.question(), null, null, null, null);

    assertThat(receipt).isEqualTo(new WriteReceipt(saved.id()));
  }

  @Test
  void editQuestion_passesTheTargetParentThrough() {
    var saved = question();
    var solutionId = UUID.randomUUID();
    when(service.editQuestion(any(), any(), any(), any(), any())).thenReturn(saved);

    tools.editQuestion(saved.id(), null, null, null, solutionId);

    verify(service).editQuestion(saved.id(), null, null, null, solutionId);
  }

  private RefinementQuestion question() {
    return new RefinementQuestion(
        UUID.randomUUID(),
        UUID.randomUUID(),
        null,
        randomString(),
        QuestionStatus.OPEN,
        null,
        null,
        null,
        Instant.now(),
        Instant.now());
  }
}
