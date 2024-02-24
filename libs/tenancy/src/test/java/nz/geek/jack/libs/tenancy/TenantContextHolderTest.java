package nz.geek.jack.libs.tenancy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class TenantContextHolderTest {

  @Test
  void set_setsContext() {
    var context = newContext();

    TenantContextHolder.set(context);
    var setContext = TenantContextHolder.get();

    assertThat(setContext).isNotEmpty();
    assertThat(setContext.get()).isEqualTo(context);
  }

  @Test
  void get_isEmptyByDefault() {
    var context = TenantContextHolder.get();

    assertThat(context).isEmpty();
  }

  @Test
  void get_returnsContext() {
    var context = newContext();

    TenantContextHolder.set(context);
    var setContext = TenantContextHolder.get();

    assertThat(setContext.get()).isEqualTo(context);
  }

  @Test
  void clear_clearsContext() {
    var context = newContext();
    TenantContextHolder.set(context);

    var beforeClear = TenantContextHolder.get();
    assertThat(beforeClear).isNotEmpty();

    TenantContextHolder.clear();

    var afterClear = TenantContextHolder.get();
    assertThat(afterClear).isEmpty();
  }

  private TenantContext newContext() {
    var tenant = newId();
    return SimpleTenantContext.of(tenant);
  }

  private String newId() {
    return UUID.randomUUID().toString();
  }
}
