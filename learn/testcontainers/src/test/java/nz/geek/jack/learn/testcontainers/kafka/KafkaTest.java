package nz.geek.jack.learn.testcontainers.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public class KafkaTest {
  @Container
  public KafkaContainer kafka =
      new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.0"));

  @Test
  public void getBootstrapServers_isNotBlank() {
    assertThat(kafka.getBootstrapServers()).isNotBlank();
  }

  @Test
  void testKafkaMessaging() {
    var topic = "test-topic";
    var message = "Hello, Kafka!";

    // Producer properties
    var producerProps = new Properties();
    producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
    producerProps.put(ProducerConfig.ACKS_CONFIG, "all");
    producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    producerProps.put(
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

    // Consumer properties
    var consumerProps = new Properties();
    consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
    consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
    consumerProps.put(
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    consumerProps.put(
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    // Ensure we start consuming from the beginning since we are starting the consume after
    // producing.
    consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    // Create a Kafka producer
    try (var producer = new KafkaProducer<String, String>(producerProps)) {
      // Produce a message to the topic
      producer.send(new ProducerRecord<>(topic, message));
      producer.flush();
    }

    // Create a Kafka consumer
    try (var consumer = new KafkaConsumer<String, String>(consumerProps)) {
      // Subscribe to the topic
      consumer.subscribe(Collections.singletonList(topic));

      // Poll for records
      var records = consumer.poll(Duration.ofSeconds(5));

      // Assert that we received the expected message
      assertThat(records.count()).isEqualTo(1);
      assertThat(records.iterator().next().value()).isEqualTo(message);
    }
  }
}
