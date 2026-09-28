package lab.gateway;

import java.util.List;
import java.util.UUID;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

/** Static identity mapping for the form-login iteration of the gateway. */
@ConfigurationProperties("lab.gateway")
public record GatewayUsers(List<ConfiguredUser> users) {
  public GatewayUsers {
    users = users == null ? List.of() : List.copyOf(users);
    Assert.isTrue(
        users.stream().map(ConfiguredUser::username).distinct().count() == users.size(),
        "Gateway usernames must be unique");
  }

  public record ConfiguredUser(
      String username, String password, UUID principal, UUID tenant, List<String> scopes) {
    public ConfiguredUser {
      Assert.hasText(username, "A gateway user needs a username");
      Assert.isTrue(
          password != null && password.matches("\\{[^}]+\\}.+"),
          "A gateway password uses Spring Security's {id}encodedPassword format");
      Assert.notNull(principal, "A gateway user needs a principal UUID");
      Assert.notNull(tenant, "A gateway user needs a tenant UUID");
      scopes = scopes == null ? List.of() : List.copyOf(scopes);
      scopes.forEach(scope -> Assert.hasText(scope, "Gateway scopes must not be blank"));
    }

    @Override
    public String toString() {
      return "ConfiguredUser[username=" + username + "]";
    }
  }
}
