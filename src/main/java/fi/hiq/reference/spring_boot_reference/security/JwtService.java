package fi.hiq.reference.spring_boot_reference.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.SimpleSecurityContext;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.JWTProcessor;
import fi.hiq.reference.spring_boot_reference.service.ClockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
public final class JwtService {
  @Value("${jwt.validity.hours}")
  private long jwtValidityHours;
  @Resource
  private DirectEncrypter directEncrypter;
  @Resource
  private JWTProcessor<SimpleSecurityContext> jwtProcessor;
  @Resource
  private ClockService clockService;

  private static final String PASSWORD_CLAIM = "pss";

  public String generateToken(UserDetails userDetails, String password) {
    Map<String, Object> claims = new HashMap<>();
    final byte[] base64Bytes;
    if (password != null) {
      base64Bytes = Base64.getEncoder().encode(password.getBytes(StandardCharsets.UTF_8));
    } else {
      base64Bytes = new byte[0];
    }

    return doGenerateToken(claims, userDetails.getUsername(), base64Bytes);
  }

  String getUsernameFromToken(String token) {
    return getClaimFromToken(token, JWTClaimsSet::getSubject);
  }

  String getPasswordFromToken(String token) {
    Object claimValue = getClaimFromToken(token, jwtClaimsSet -> jwtClaimsSet.getClaim(PASSWORD_CLAIM));
    if (claimValue != null) {
      byte[] passwordBytes = Base64.getDecoder().decode(claimValue.toString().getBytes(StandardCharsets.UTF_8));
      return new String(passwordBytes, StandardCharsets.UTF_8);
    } else {
      return null;
    }
  }

  boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = getUsernameFromToken(token);
    return (username.equals(userDetails.getUsername()) && isTokenTimeframeValid(token));
  }

  boolean isTokenExpired(String token) {
    final Date expirationTime = getClaimFromToken(token, JWTClaimsSet::getExpirationTime);
    // JWT times are stored in seconds, rounded down
    final Date now = new Date(clockService.nowDate().getTime() / 1000 * 1000);
    return expirationTime == null || expirationTime.before(now);
  }

  JWTClaimsSet getAllClaimsFromToken(String token) {
    try {
      return jwtProcessor.process(token, null);
    } catch (ParseException | BadJOSEException | JOSEException e) {
      return new JWTClaimsSet.Builder().build();
    }
  }

  boolean isTokenStartTimeInTheFuture(String token) {
    // JWT times are stored in seconds, rounded down
    final Date now = new Date(clockService.nowDate().getTime() / 1000 * 1000);
    final Date issueTime = getClaimFromToken(token, JWTClaimsSet::getIssueTime);
    final Date notBeforeTime = getClaimFromToken(token, JWTClaimsSet::getNotBeforeTime);

    return issueTime.after(now) || notBeforeTime.after(now);
  }

  <T> T getClaimFromToken(String token, Function<JWTClaimsSet, T> claimsResolver) {
    final JWTClaimsSet claims = getAllClaimsFromToken(token);
    return claimsResolver.apply(claims);
  }

  String doGenerateToken(Map<String, Object> claims, String subject, byte[] passwordBytes) {
    JWTClaimsSet.Builder jwtClaimSetBuilder = new JWTClaimsSet.Builder()
        .claim(PASSWORD_CLAIM, new String(passwordBytes, StandardCharsets.UTF_8));

    claims.forEach(jwtClaimSetBuilder::claim);

    final Date now = clockService.nowDate();
    JWTClaimsSet jwtClaimsSet = jwtClaimSetBuilder
        .subject(subject)
        .notBeforeTime(now)
        .issueTime(now)
        .expirationTime(new Date(now.getTime() + jwtValidityHours * 60 * 60 * 1000))
        .build();

    EncryptedJWT jwt = new EncryptedJWT(JwtEncrypter.JWE_HEADER, jwtClaimsSet);
    try {
      jwt.encrypt(directEncrypter);
    } catch (JOSEException | IllegalStateException e) {
      log.error("Error while encrypting JWE object: ", e);
      return null;
    }

    return jwt.serialize();
  }

  private boolean isTokenTimeframeValid(String token) {
    return !isTokenExpired(token) && !isTokenStartTimeInTheFuture(token);
  }
}
