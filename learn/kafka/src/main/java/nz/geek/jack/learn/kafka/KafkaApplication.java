package nz.geek.jack.learn.kafka;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class KafkaApplication {

  private final Producer producer;

  public KafkaApplication(Producer producer) {
    this.producer = producer;
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
        this.producer.sendMessage("key", arg);
      }
    };
  }
}
