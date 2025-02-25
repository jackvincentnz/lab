package nz.geek.jack.mops;

import nz.geek.jack.libs.tenancy.filter.TenantFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MopsApplication {

  public static void main(String[] args) {
    SpringApplication.run(MopsApplication.class, args);
  }

  @Bean
  @Order(1)
  TenantFilter tenantFilter() {
    return new TenantFilter();
  }
}
