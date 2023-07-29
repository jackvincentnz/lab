package nz.geek.jack.learn.kafka;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;

@SpringBootApplication
public class KafkaApplication {

  private final Producer producer;

  private final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

  public KafkaApplication(
      Producer producer, KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry) {
    this.producer = producer;
    this.kafkaListenerEndpointRegistry = kafkaListenerEndpointRegistry;
  }

  public static void main(String[] args) {
    SpringApplication application = new SpringApplication(KafkaApplication.class);
    application.setWebApplicationType(WebApplicationType.NONE);
    application.run(args);
  }

  @Bean
  public CommandLineRunner CommandLineRunnerBean() {
    return (args) -> {
      for (String arg : args) {
        switch (arg) {
          case "--producer":
            this.producer.sendMessage("awalther", "t-shirts");
            this.producer.sendMessage("htanaka", "t-shirts");
            this.producer.sendMessage("htanaka", "batteries");
            this.producer.sendMessage("eabara", "t-shirts");
            this.producer.sendMessage("htanaka", "t-shirts");
            this.producer.sendMessage("jsmith", "book");
            this.producer.sendMessage("awalther", "t-shirts");
            this.producer.sendMessage("jsmith", "batteries");
            this.producer.sendMessage("jsmith", "gift card");
            this.producer.sendMessage("eabara", "t-shirts");
            break;
          case "--consumer":
            MessageListenerContainer listenerContainer =
                kafkaListenerEndpointRegistry.getListenerContainer("myConsumer");
            listenerContainer.start();
            break;
          default:
            break;
        }
      }
    };
  }
}
