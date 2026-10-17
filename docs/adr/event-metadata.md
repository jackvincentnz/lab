# Event metadata

## Status

Proposed.

Date: 2026-10-16
Decision owner: Jack Vincent

## Context

A domain event records something that happened in the domain, such as a completed task. Mops
handles its events in process: Spring publishes them from the aggregate with `@DomainEvents` to
`@EventListener` methods in the same JVM, and an `@Async` listener's work is lost if the process
stops first. No broker is involved, so this record does not cover them.

The Organizer task service publishes its events to Kafka, and autojournal consumes them. Kafka with
Spring Kafka's defaults delivers at least once: a consumer commits its offset after processing, so
a crash or rebalance between the two makes it process the same record again. Each consumer must
therefore be an idempotent consumer (Enterprise Integration Patterns calls it an idempotent
receiver), which has the same effect however many times a record arrives. The usual way is a
unique id per event, which the consumer records in the same transaction as its own changes.

The record value is the protobuf event in Schema Registry framing, which also tells a consumer the
event's type. Nothing tells one emission from another. Data about an event that travels with it but
is not part of it, such as this id, is called an envelope. The question is where the envelope goes.

## Decision

Carry event metadata as Kafka record headers. The record value stays the protobuf event written by
the Schema Registry serializer, and `DomainEvent` does not change. Header values are UTF-8 strings.

Now:

- `event-id`: a random UUID in canonical form, minted once per event by the publishing adapter.

Reserved names, added when a consumer needs them:

- `event-type`: the protobuf full name, such as `task.TaskCompletedEvent`, for tools that do not
  read Schema Registry.
- `occurred-at`: an RFC 3339 UTC timestamp of when the event happened, taken from the domain event,
  not the time of publishing.

No other headers. Correlation and causation ids, which link a chain of events to the request that
started it and to the event that caused each one, earn a place when one service's event makes
another service emit one. Trace context belongs to tracing instrumentation and its W3C
`traceparent` header, not to this envelope.

This is the shape of CloudEvents binary content mode, where the event data is the record value and
its attributes are headers, without the `ce_` names or the required `source` and `specversion`.
Headers are where Kafka keeps record metadata, so it need not be packed into the key or value. They
can be read without protobuf descriptors or registry access, by people and by tools such as kcat,
Kafka UI and Spring Kafka's `RecordFilterStrategy`.

The id identifies one emission of an event to a broker, which only the adapter, or an outbox where
one exists, can make durable, so it stays out of the domain model. When the event happened is a
domain fact, so `occurred-at` comes from the domain event.

## Consequences

Consumers can deduplicate on `event-id` without parsing the value. Each consumer stores the ids it
has processed in the same transaction as its effects, and chooses how long to keep them.

Kafka producer retries resend the same record, so they keep its id. The task service writes its
store and Kafka separately, the dual-write problem, so an event can be published for a save that
then fails, and an application-level republish gets a new id that deduplication cannot catch. A
transactional outbox fixes both: it holds the header values as columns beside the payload bytes,
and the relay copies them into headers, so it needs no envelope type on the wire.

A consumer that requires the header rejects records produced without it. Header names are a
contract with no schema, so `libs/messaging` holds them for both sides.

## Alternatives considered

- **Protobuf envelope with an `Any` payload.** Keeps metadata and payload in one value that
  survives outside Kafka, such as in files. Every consumer must know the packed types to unpack
  them, Schema Registry checks only the envelope's compatibility rather than each event's, and the
  metadata is hidden from tools without descriptors.
- **CloudEvents binary mode.** Gives a published standard, searchable names and SDKs. Costs the
  `source` and `specversion` headers and an SDK or mapping in each service, for traffic between two
  services in one repository. Structured mode, which puts attributes and data together in a JSON
  value, would also replace the protobuf value.
- **Id and timestamp on `DomainEvent`.** Gives one id from the moment the event happens, visible in
  tests and logs, as in many DDD examples. Puts a messaging identity in the domain model, and
  without an event store or outbox the id is no more durable than one minted by the adapter.
- **Id derived from aggregate id and version.** Deterministic, so republishing keeps the id
  without an outbox. Only event-sourced aggregates have a version per event, and the id then
  depends on how events are stored.

## Reconsider when

- Events are stored or forwarded outside Kafka, where headers are lost: evaluate an envelope in the
  value.
- A consumer cannot read Schema Registry framing: add `event-type`, or evaluate an envelope.
- A producer gains an outbox or event store: mint `event-id` there so a republish keeps it.
- A party outside this repository consumes the events: adopt CloudEvents for that contract.

## References

- [CloudEvents specification](https://github.com/cloudevents/spec/blob/main/cloudevents/spec.md).
- [CloudEvents Kafka protocol binding](https://github.com/cloudevents/spec/blob/main/cloudevents/bindings/kafka-protocol-binding.md).
- [KIP-82: Add Record Headers](https://cwiki.apache.org/confluence/display/KAFKA/KIP-82+-+Add+Record+Headers).
- [Kafka message delivery guarantees](https://docs.confluent.io/kafka/design/delivery-semantics.html).
- [Schema Registry wire format](https://docs.confluent.io/platform/current/schema-registry/fundamentals/serdes-develop/index.html).
- [Idempotent Receiver](https://www.enterpriseintegrationpatterns.com/patterns/messaging/IdempotentReceiver.html).
- [Idempotent consumer](https://microservices.io/patterns/communication-style/idempotent-consumer.html).
- [Transactional outbox](https://microservices.io/patterns/data/transactional-outbox.html).
- [Debezium outbox event router](https://debezium.io/documentation/reference/stable/transformations/outbox-event-router.html).
- [Spring Kafka filtering messages](https://docs.spring.io/spring-kafka/reference/kafka/receiving-messages/filtering.html).
- [Domain Event, Martin Fowler](https://martinfowler.com/eaaDev/DomainEvent.html).
