package nz.geek.jack.task.infrastructure.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "nz.geek.jack.task")
public class TaskApplication {

  public static void main(String[] args) {
    SpringApplication.run(nz.geek.jack.task.infrastructure.web.TaskApplication.class, args);
  }
}
