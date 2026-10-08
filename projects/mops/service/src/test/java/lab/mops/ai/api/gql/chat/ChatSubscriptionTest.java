package lab.mops.ai.api.gql.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import lab.mops.ai.application.chat.ChatQueryService;
import lab.mops.ai.domain.chat.Chat;
import lab.mops.ai.domain.chat.ChatId;
import lab.mops.ai.domain.chat.ChatMessageCompletedEvent;
import lab.mops.ai.domain.chat.MessageId;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.BaseSubscriber;

@ExtendWith(MockitoExtension.class)
class ChatSubscriptionTest extends TestBase {
  @Mock ChatQueryService queries;
  @Mock ChatMapper mapper;
  @InjectMocks ChatSubscription subscription;

  @Test
  void chatUpdated_sendsCurrentStateThenUpdatesOnlyThatChat() {
    var chat = Chat.start(randomString());
    var initial = lab.mops.api.gql.types.Chat.newBuilder().id(randomId()).build();
    var updated = lab.mops.api.gql.types.Chat.newBuilder().id(initial.getId()).build();
    when(queries.getById(chat.getId())).thenReturn(chat);
    when(mapper.map(chat)).thenReturn(initial, updated);
    var received = new ArrayList<lab.mops.api.gql.types.Chat>();
    var connection = subscription.chatUpdated(chat.getId().toString()).subscribe(received::add);

    subscription.onChatChanged(
        new ChatMessageCompletedEvent(ChatId.create(), MessageId.create(), randomString()));
    assertThat(received).containsExactly(initial);
    subscription.onChatChanged(
        new ChatMessageCompletedEvent(chat.getId(), MessageId.create(), randomString()));
    assertThat(received).containsExactly(initial, updated);
    connection.dispose();
  }

  @Test
  void chatUpdated_disposalRemovesSubscriberAndReconnectReadsFreshSnapshot() {
    var chat = Chat.start(randomString());
    var snapshot = lab.mops.api.gql.types.Chat.newBuilder().id(randomId()).build();
    when(queries.getById(chat.getId())).thenReturn(chat);
    when(mapper.map(chat)).thenReturn(snapshot);
    var connection = subscription.chatUpdated(chat.getId().toString()).subscribe();
    connection.dispose();
    subscription.onChatChanged(
        new ChatMessageCompletedEvent(chat.getId(), MessageId.create(), randomString()));
    verify(queries).getById(chat.getId());
    var received = new ArrayList<lab.mops.api.gql.types.Chat>();
    var reconnected = subscription.chatUpdated(chat.getId().toString()).subscribe(received::add);
    assertThat(received).containsExactly(snapshot);
    reconnected.dispose();
  }

  @Test
  void chatUpdated_slowSubscriberReceivesLatestSnapshotWithoutUnboundedBuffering() {
    var chat = Chat.start(randomString());
    var initial = lab.mops.api.gql.types.Chat.newBuilder().id(randomId()).build();
    var latest = lab.mops.api.gql.types.Chat.newBuilder().id(initial.getId()).build();
    when(queries.getById(chat.getId())).thenReturn(chat);
    when(mapper.map(chat)).thenReturn(initial, latest);
    var received = new ArrayList<lab.mops.api.gql.types.Chat>();
    var connection =
        new BaseSubscriber<lab.mops.api.gql.types.Chat>() {
          @Override
          protected void hookOnSubscribe(org.reactivestreams.Subscription ignored) {}

          @Override
          protected void hookOnNext(lab.mops.api.gql.types.Chat value) {
            received.add(value);
          }
        };
    subscription.chatUpdated(chat.getId().toString()).subscribe(connection);
    subscription.onChatChanged(
        new ChatMessageCompletedEvent(chat.getId(), MessageId.create(), randomString()));
    connection.request(1);
    assertThat(received).containsExactly(latest);
    connection.dispose();
  }

  @Test
  void chatUpdated_twoClients_bothReceiveUpdatesAndDisposeIndependently() {
    var chat = Chat.start(randomString());
    var snapshot = lab.mops.api.gql.types.Chat.newBuilder().id(randomId()).build();
    when(queries.getById(chat.getId())).thenReturn(chat);
    when(mapper.map(chat)).thenReturn(snapshot);
    var first = new ArrayList<lab.mops.api.gql.types.Chat>();
    var second = new ArrayList<lab.mops.api.gql.types.Chat>();
    var firstConnection = subscription.chatUpdated(chat.getId().toString()).subscribe(first::add);
    var secondConnection = subscription.chatUpdated(chat.getId().toString()).subscribe(second::add);
    var event = new ChatMessageCompletedEvent(chat.getId(), MessageId.create(), randomString());
    subscription.onChatChanged(event);
    assertThat(first).containsExactly(snapshot, snapshot);
    assertThat(second).containsExactly(snapshot, snapshot);
    firstConnection.dispose();
    subscription.onChatChanged(event);
    assertThat(first).hasSize(2);
    assertThat(second).hasSize(3);
    secondConnection.dispose();
  }

  @Test
  void chatUpdated_missingChatFailsWithoutKeepingAListener() {
    var id = ChatId.create();
    when(queries.getById(id)).thenThrow(new IllegalArgumentException("missing chat"));
    var errors = new ArrayList<Throwable>();
    subscription.chatUpdated(id.toString()).subscribe(ignored -> {}, errors::add);
    assertThat(errors).hasSize(1);
    subscription.onChatChanged(
        new ChatMessageCompletedEvent(id, MessageId.create(), randomString()));
    verify(queries).getById(id);
  }
}
