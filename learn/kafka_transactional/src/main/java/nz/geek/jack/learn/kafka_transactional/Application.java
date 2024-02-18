package nz.geek.jack.learn.kafka_transactional;

import javax.sql.DataSource;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@SpringBootApplication
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Bean
  public ApplicationRunner runner(KafkaTemplate<String, String> template) {
    return args -> template.executeInTransaction(t -> t.send("topic1", "test"));
  }

  /*
   * @Primary to avoid having to specify "dtsm" on every @Transactional where we want a datasource transaction
   * E.g. "expected single matching bean but found 2: dstm,kafkaTransactionManager"
   */
  @Bean
  @Primary
  public DataSourceTransactionManager dstm(DataSource dataSource) {
    return new DataSourceTransactionManager(dataSource);
  }

  @Component
  public static class Listener {

    private final JdbcTemplate jdbcTemplate;

    private final KafkaTemplate<String, String> kafkaTemplate;

    public Listener(JdbcTemplate jdbcTemplate, KafkaTemplate<String, String> kafkaTemplate) {
      this.jdbcTemplate = jdbcTemplate;
      this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(id = "group1", topics = "topic1")
    @Transactional
    public void listen1(String in) {
      this.kafkaTemplate.send("topic2", in.toUpperCase());
      this.jdbcTemplate.execute("insert into mytable (data) values ('" + in + "')");
    }

    @KafkaListener(id = "group2", topics = "topic2")
    public void listen2(String in) {
      System.out.println(in);
    }
  }

  @Bean
  public NewTopic topic1() {
    return TopicBuilder.name("topic1").build();
  }

  @Bean
  public NewTopic topic2() {
    return TopicBuilder.name("topic2").build();
  }
}
