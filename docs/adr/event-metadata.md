# Event metadata

## Status

Proposed.

Date: 2026-10-16
Decision owner: Jack Vincent

## Context

In-process events, published with Spring `@DomainEvents` and handled by `@EventListener` as in
Mops, run synchronously or can be dropped. Events that must survive a restart or cross a service
boundary go through a message broker, and later an outbox. Kafka delivers at least once, so
consumers of broker-carried events need an id to tell a redelivery from a new event.

## Decision

Carry event metadata as Kafka record headers and keep the record value the bare protobuf event.
This has the shape of CloudEvents binary mode without its names or spec. Start with the minimum:

- `event-id`: a UUID the publisher mints once per event.
- `event-type`: the protobuf full name, such as `task.TaskCompletedEvent`. Added when needed.
- `occurred-at`: an ISO-8601 instant. Added when needed.

Add a header only when a consumer needs it. Correlation and causation ids wait for a second hop,
and instrumentation already propagates trace context. Event identity is not a domain concern: the
envelope belongs to the messaging adapter, and `DomainEvent` does not carry it.

Headers are readable without protobuf descriptors, by people and by payload-agnostic tools such as
kcat, Kafka UI, Spring Kafka's `RecordFilterStrategy` and broker-side filters. Kafka already keeps
routing outside the payload, in the topic, key and headers.

## Consequences

Consumers can deduplicate on `event-id`. A publisher without an outbox mints a new id when it
republishes an event. An outbox table, when added, holds the header values as columns beside the
payload bytes, so it needs no wire-level envelope type.

## Alternatives considered

- **Protobuf envelope with an `Any` payload.** One language-neutral blob, but it adds a type
  registry and hides metadata from tools without descriptors. An outbox row already holds what it
  would carry.
- **CloudEvents structured or binary mode.** Right for a public boundary, but extra spec weight for
  internal service-to-service traffic.
- **Id and timestamp on `DomainEvent`.** Puts the identity of an emission in the domain model.

## Reconsider when

- A non-Java consumer appears, or events are replayed from files: evaluate a protobuf envelope.
- Several event types must share a topic and describe themselves: evaluate a protobuf envelope.
- A party outside this repository consumes the events: adopt CloudEvents for that contract.

## References

- [CloudEvents Kafka protocol binding](https://github.com/cloudevents/spec/blob/main/cloudevents/bindings/kafka-protocol-binding.md).
- [KIP-82: Add Record Headers](https://cwiki.apache.org/confluence/display/KAFKA/KIP-82+-+Add+Record+Headers).
