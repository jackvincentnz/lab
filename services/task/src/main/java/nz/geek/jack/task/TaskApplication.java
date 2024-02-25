package nz.geek.jack.task;

import nz.geek.jack.libs.tenancy.filter.TenantFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

@SpringBootApplication()
public class TaskApplication {

  public static void main(String[] args) {
    SpringApplication.run(TaskApplication.class, args);
  }

  @Bean
  @Order(1)
  TenantFilter tenantFilter() {
    return new TenantFilter();
  }
}
