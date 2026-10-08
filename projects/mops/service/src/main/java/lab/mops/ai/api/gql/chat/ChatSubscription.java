package lab.mops.ai.api.gql.chat;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsSubscription;
import com.netflix.graphql.dgs.InputArgument;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lab.mops.ai.application.chat.ChatQueryService;
import lab.mops.ai.domain.chat.ChatChangedEvent;
import lab.mops.ai.domain.chat.ChatId;
import lab.mops.api.gql.types.Chat;
import org.springframework.transaction.event.TransactionalEventListener;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

@DgsComponent
public class ChatSubscription {

  private final ChatQueryService chatQueryService;
  private final ChatMapper chatMapper;
  private final Map<ChatId, Set<FluxSink<Chat>>> subscribers = new HashMap<>();

  public ChatSubscription(ChatQueryService chatQueryService, ChatMapper chatMapper) {
    this.chatQueryService = chatQueryService;
    this.chatMapper = chatMapper;
  }

  @DgsSubscription
  public Flux<Chat> chatUpdated(@InputArgument("id") String id) {
    var chatId = ChatId.fromString(id);
    return Flux.create(
        sink -> {
          synchronized (subscribers) {
            // Register and read under the same lock as updates so connection/reconnection
            // cannot miss a completion between the initial snapshot and the live stream.
            var snapshot = snapshot(chatId);
            subscribers.computeIfAbsent(chatId, ignored -> new HashSet<>()).add(sink);
            sink.onDispose(() -> unsubscribe(chatId, sink));
            sink.next(snapshot);
          }
        },
        // Each payload contains the whole chat, so a slow client only needs the latest state.
        FluxSink.OverflowStrategy.LATEST);
  }

  @TransactionalEventListener(fallbackExecution = true)
  public void onChatChanged(ChatChangedEvent event) {
    synchronized (subscribers) {
      var sinks = subscribers.get(event.chatId());
      if (sinks == null) return;
      var snapshot = snapshot(event.chatId());
      // Disposal can run synchronously while delivering a payload.
      for (var sink : Set.copyOf(sinks)) {
        sink.next(snapshot);
      }
    }
  }

  private Chat snapshot(ChatId chatId) {
    return chatMapper.map(chatQueryService.getById(chatId));
  }

  private void unsubscribe(ChatId chatId, FluxSink<Chat> sink) {
    synchronized (subscribers) {
      var sinks = subscribers.get(chatId);
      if (sinks == null) return;
      sinks.remove(sink);
      if (sinks.isEmpty()) subscribers.remove(chatId);
    }
  }
}
