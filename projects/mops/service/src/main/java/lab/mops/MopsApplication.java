package lab.mops;

import nz.geek.jack.libs.tenancy.filter.TenantFilter;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@ConfigurationPropertiesScan
@RestController
@EnableAsync
public class MopsApplication {

  private static ConfigurableApplicationContext context;

  public static void main(String[] args) {
    context = SpringApplication.run(MopsApplication.class, args);
  }

  @GetMapping("/reset")
  public void reset() {
    ApplicationArguments args = context.getBean(ApplicationArguments.class);

    Thread thread =
        new Thread(
            () -> {
              context.close();
              context = SpringApplication.run(MopsApplication.class, args.getSourceArgs());
            });

    thread.setDaemon(false);
    thread.start();
  }

  @Bean
  @Order(1)
  TenantFilter tenantFilter() {
    return new TenantFilter();
  }
}
