package lab.mops.config.client;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import lab.mops.api.gql.types.ClientConfiguration;

@DgsComponent
public class ConfigDataFetcher {

  private final StatsigProperties statsigProperties;

  public ConfigDataFetcher(StatsigProperties statsigProperties) {
    this.statsigProperties = statsigProperties;
  }

  @DgsQuery
  public ClientConfiguration clientConfiguration() {
    var configuration = ClientConfiguration.newBuilder();

    statsigProperties.getClientKey().ifPresent(configuration::statsigKey);

    return configuration.build();
  }
}
