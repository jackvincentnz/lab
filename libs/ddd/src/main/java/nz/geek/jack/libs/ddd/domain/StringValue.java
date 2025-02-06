package nz.geek.jack.libs.ddd.domain;

import java.util.Objects;

public abstract class StringValue {

  protected final String value;

  protected StringValue(String value) {
    Objects.requireNonNull(value);
    this.value = value;
  }

  @Override
  public String toString() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    StringValue that = (StringValue) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(value);
  }
}
