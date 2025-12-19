package lab.mops.ai.api.gql.chat;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsSubscription;
import java.time.Duration;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

@DgsComponent
public class SubscriptionDataFetcher {

  @DgsSubscription
  public Publisher<Long> counter() {
    return Flux.interval(Duration.ofSeconds(0), Duration.ofSeconds(1));
  }
}
