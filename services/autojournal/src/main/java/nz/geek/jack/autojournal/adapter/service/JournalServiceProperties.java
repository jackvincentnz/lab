package nz.geek.jack.autojournal.adapter.service;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Validated
@Configuration
@ConfigurationProperties(prefix = "lab.services.journal")
public class JournalServiceProperties {

  @NotBlank private String graphqlUrl;

  public String getGraphqlUrl() {
    return graphqlUrl;
  }

  public void setGraphqlUrl(String graphqlUrl) {
    this.graphqlUrl = graphqlUrl;
  }
}
