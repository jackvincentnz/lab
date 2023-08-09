package nz.geek.jack.autojournal.infrastructure.service;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Validated
@Configuration
@ConfigurationProperties(prefix = "lab.services.task")
public class TaskServiceProperties {

  @NotBlank private String graphqlUrl;

  public String getGraphqlUrl() {
    return graphqlUrl;
  }

  public void setGraphqlUrl(String graphqlUrl) {
    this.graphqlUrl = graphqlUrl;
  }
}
