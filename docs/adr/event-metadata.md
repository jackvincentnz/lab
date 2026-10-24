# Event metadata

## Status

Proposed.

Date: 2026-10-16
Decision owner: Jack Vincent

## Context

The Organizer task service publishes its domain events to Kafka, and autojournal consumes them.
Kafka with Spring Kafka's defaults delivers at least once: a crash or rebalance between processing
a record and committing its offset makes the consumer process it again. A consumer that must have
the same effect however often a record arrives, an idempotent consumer, usually records a unique id
per event in the same transaction as its own changes.

The record value is the protobuf event in Schema Registry framing, which tells a consumer the
event's type but nothing that tells one emission from another. Data that travels with an event but
is not part of it, such as this id, is called an envelope. The question is where the envelope goes.

Mops handles its domain events in process with Spring's `@EventListener`, with no broker, so this
record does not cover them.

## Decision

Carry event metadata as Kafka record headers. The record value stays the protobuf event written by
the Schema Registry serializer, and `DomainEvent` does not change. Header names are lowercase and
hyphenated, and values are UTF-8 strings.

The `event-id` header holds a random UUID in canonical form, minted once per record by the
publishing adapter. The id identifies one emission to a broker, which only the adapter, or an
outbox where one exists, can make durable, so it stays out of the domain model.

This is the shape of CloudEvents binary content mode, where attributes are headers and the event
is the value, without the `ce_` names or the required `source` and `specversion`. Headers can be
read without protobuf descriptors or registry access, by people and by tools such as kcat and
Spring Kafka's `RecordFilterStrategy`. Trace context stays in the W3C `traceparent` header that
tracing instrumentation writes.

The cost is a contract with no schema: the producer and each consumer write the header name, and
this record is where it is defined.

## Consequences

The header alone deduplicates nothing. A consumer deduplicates by storing the ids it has processed
in the same transaction as its effects, and chooses how long to keep them.

Kafka producer retries resend the same record, so they keep its id. The task service writes its
store and Kafka separately, so an event can be published for a save that then fails, and an
application-level republish gets a new id that deduplication cannot catch. A transactional outbox
fixes both: it stores the header values beside the payload, and its relay copies them into
headers.

A topic keeps records published before the header existed, and autojournal reads from the earliest
offset when its consumer group is new. Its error handler stops the listener on any failure, so a
required header would stop it at the first such record and again after every restart. A consumer
therefore reads `event-id` as optional and processes a record without one, which it cannot
deduplicate.

## Alternatives considered

- **`event_id` field in each protobuf message.** Typed, checked by Schema Registry, and kept when
  events leave Kafka. Every message repeats the field, and the domain-to-protobuf mappers must mint
  the id.
- **Protobuf envelope with an `Any` payload.** Keeps metadata and payload in one value. Every
  consumer must know the packed types to unpack them, Schema Registry checks only the envelope's
  compatibility rather than each event's, and the metadata is hidden from tools without
  descriptors.
- **CloudEvents binary mode.** Gives a published standard and SDKs. Costs the `source` and
  `specversion` headers and an SDK or mapping in each service, for traffic between two services in
  one repository.
- **Id on `DomainEvent`.** Gives one id from the moment the event happens, visible in tests and
  logs. Puts a messaging identity in the domain model, and without an event store or outbox the id
  is no more durable than one minted by the adapter.
- **Id derived from aggregate id and version.** Keeps the id on republish without an outbox. Only
  a stored event stream numbers each event, and the task service keeps its aggregates in memory,
  where the version stays at zero.
- **Shared library for header names.** Lets the compiler keep producer and consumer in step. For
  one header with one producer, a library, README, and test target cost more than a constant in
  each adapter.

## Reconsider when

- Events are stored or forwarded outside Kafka, where headers are lost: evaluate a field or an
  envelope in the value.
- A consumer cannot read Schema Registry framing: add an `event-type` header with the protobuf
  full name, or evaluate an envelope.
- A consumer needs when the event happened rather than the record timestamp: add an `occurred-at`
  header in RFC 3339 UTC, taken from the domain event.
- One service's event makes another service emit one: add correlation and causation id headers.
- A producer gains an outbox or event store: mint `event-id` there so a republish keeps it.
- Several services read or write the same headers: evaluate a shared library for their names.
- A party outside this repository consumes the events: publish them through a separate public
  contract, using CloudEvents, and leave the internal headers as they are.

## References

- [CloudEvents Kafka protocol binding](https://github.com/cloudevents/spec/blob/main/cloudevents/bindings/kafka-protocol-binding.md).
- [KIP-82: Add Record Headers](https://cwiki.apache.org/confluence/display/KAFKA/KIP-82+-+Add+Record+Headers).
- [Kafka message delivery guarantees](https://docs.confluent.io/kafka/design/delivery-semantics.html).
- [Schema Registry wire format](https://docs.confluent.io/platform/current/schema-registry/fundamentals/serdes-develop/index.html).
- [Idempotent consumer](https://microservices.io/patterns/communication-style/idempotent-consumer.html).
- [Transactional outbox](https://microservices.io/patterns/data/transactional-outbox.html).
- [Spring Kafka error handlers](https://docs.spring.io/spring-kafka/reference/kafka/annotation-error-handling.html).
