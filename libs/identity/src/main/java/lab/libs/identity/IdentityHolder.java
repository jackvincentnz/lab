package lab.libs.identity;

import java.util.Optional;

/** Holds the identity of the request being handled on the current thread. */
public final class IdentityHolder {

  private static final ThreadLocal<Identity> CURRENT = new ThreadLocal<>();

  private IdentityHolder() {}

  public static void set(Identity identity) {
    CURRENT.set(identity);
  }

  public static Optional<Identity> get() {
    return Optional.ofNullable(CURRENT.get());
  }

  public static void clear() {
    CURRENT.remove();
  }
}
