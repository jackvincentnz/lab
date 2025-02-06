package nz.geek.jack.mops.config;

import java.util.List;
import nz.geek.jack.libs.ddd.domain.springdata.Converters;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;

@Configuration
public class DatabaseConfig extends AbstractJdbcConfiguration {
  @Override
  protected List<?> userConverters() {
    return Converters.allConverters();
  }
}
