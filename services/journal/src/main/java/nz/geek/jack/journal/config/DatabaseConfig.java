package nz.geek.jack.journal.config;

import java.util.Arrays;
import java.util.List;
import javax.sql.DataSource;
import nz.geek.jack.libs.ddd.domain.springdata.AbstractIdToUUIDConverter;
import nz.geek.jack.libs.ddd.domain.springdata.UUIDToAbstractIdConverterFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableJdbcRepositories(basePackages = "nz.geek.jack.journal")
public class DatabaseConfig extends AbstractJdbcConfiguration {

  @Bean
  NamedParameterJdbcOperations operations() {
    return new NamedParameterJdbcTemplate(dataSource());
  }

  @Bean
  PlatformTransactionManager transactionManager() {
    return new DataSourceTransactionManager(dataSource());
  }

  @Bean
  DataSource dataSource() {
    return new EmbeddedDatabaseBuilder()
        .generateUniqueName(true)
        .setType(EmbeddedDatabaseType.H2)
        .addScript("schema.sql")
        .build();
  }

  @Override
  protected List<?> userConverters() {
    return Arrays.asList(new AbstractIdToUUIDConverter(), new UUIDToAbstractIdConverterFactory());
  }
}
