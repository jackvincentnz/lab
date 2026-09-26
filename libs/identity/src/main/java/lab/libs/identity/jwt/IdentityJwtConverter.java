package lab.libs.identity.jwt;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

/** Turns a validated identity token into the authenticated identity for the request. */
public final class IdentityJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  @Override
  public IdentityAuthentication convert(Jwt jwt) {
    return new IdentityAuthentication(IdentityClaims.identity(jwt));
  }
}
