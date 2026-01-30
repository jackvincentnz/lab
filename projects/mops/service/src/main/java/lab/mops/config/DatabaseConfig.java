package lab.mops.config;

import java.util.List;
import lab.libs.ddd.domain.springdata.Converters;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;

@Configuration
public class DatabaseConfig extends AbstractJdbcConfiguration {
  @Override
  protected List<?> userConverters() {
    return Converters.allConverters();
  }
}
