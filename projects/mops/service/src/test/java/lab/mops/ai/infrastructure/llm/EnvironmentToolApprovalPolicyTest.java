package lab.mops.ai.infrastructure.llm;

import static lab.mops.ai.infrastructure.llm.EnvironmentToolApprovalPolicy.getKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import lab.test.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

@ExtendWith(MockitoExtension.class)
class EnvironmentToolApprovalPolicyTest extends TestBase {

  @Mock Environment environment;

  @InjectMocks EnvironmentToolApprovalPolicy policy;

  @Test
  void needsApproval_shouldReturnValueFromEnvironment() {
    var tool = randomString();
    var key = getKey(tool);
    when(environment.getProperty(key, Boolean.class, true)).thenReturn(true);

    var result = policy.needsApproval(tool);

    assertThat(result).isTrue();
  }
}
