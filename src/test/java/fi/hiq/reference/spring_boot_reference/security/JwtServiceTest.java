package fi.hiq.reference.spring_boot_reference.security;

import com.nimbusds.jwt.JWTClaimsSet;
import fi.hiq.reference.spring_boot_reference.service.ClockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.util.ReflectionTestUtils;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
    "jwt.secret=EC30895A0CA01E89414AA7D3730DD93C",
    "jwt.validity.hours=1"
})
class JwtServiceTest {
  @Resource
  private JwtService jwtService;
  @MockBean
  private ClockService clockService;

  private final String username = "käyttäjä";
  private final String password = "salasana";
  private User user;

  @BeforeEach
  void setup() {
    user = mock(User.class);
    when(user.getUsername()).thenReturn(username);

    when(clockService.nowDate()).thenReturn(new Date());
  }

  @Test
  void testGenerateToken() {
    String token = jwtService.generateToken(user, password);
    assertNotNull(token);
    assertFalse(token.isEmpty());
    assertTrue(jwtService.isTokenValid(token, user));
  }

  @Test
  void testGetUsernameFromToken() {
    String token = jwtService.generateToken(user, password);

    String usernameFromToken = jwtService.getUsernameFromToken(token);
    assertEquals(username, usernameFromToken);
  }

  @Test
  void testGetPasswordFromToken() {
    String token = jwtService.generateToken(user, password);

    String passwordFromToken = jwtService.getPasswordFromToken(token);
    assertEquals(password, passwordFromToken);
  }

  @Test
  void testIsTokenExpired_expirationTimeNotPassed() {
    String token = jwtService.generateToken(user, password);

    boolean isExpired = jwtService.isTokenExpired(token);
    assertFalse(isExpired);
  }

  @Test
  void testIsTokenExpired_expirationTimePassed() {
    // Set jwt validity to -1 hours for this test so the generated token is expired
    ReflectionTestUtils.setField(jwtService, "jwtValidityHours", -1L);

    try {
      String token = jwtService.generateToken(user, password);
      boolean isExpired = jwtService.isTokenExpired(token);
      assertTrue(isExpired);
    } finally {
      // At the end of the test reset jwt validity to where it was
      ReflectionTestUtils.setField(jwtService, "jwtValidityHours", 1L);
    }

  }

  @Test
  void testGetAllClaimsFromToken() throws ParseException {
    Date now = new Date();
    when(clockService.nowDate()).thenReturn(now);

    // JWT times are stored in seconds, rounded down.
    // So for comparison use rounded time
    Date roundedNow = new Date(now.getTime() / 1000 * 1000);

    String token = jwtService.generateToken(user, password);

    JWTClaimsSet claimsSet = jwtService.getAllClaimsFromToken(token);

    String passwordClaim = claimsSet.getStringClaim("pss");
    assertNotNull(passwordClaim);
    assertNotEquals(password, passwordClaim);

    String usernameFromClaim = claimsSet.getSubject();
    assertEquals(username, usernameFromClaim);

    Date issueTime = claimsSet.getIssueTime();
    assertNotNull(issueTime);
    assertEquals(roundedNow.getTime(), issueTime.getTime());

    Date notBeforeTime = claimsSet.getNotBeforeTime();
    assertNotNull(notBeforeTime);
    assertEquals(issueTime.getTime(), notBeforeTime.getTime());

    Date expireTime = claimsSet.getExpirationTime();
    assertNotNull(expireTime);
    assertEquals(roundedNow.getTime() + 60 * 60 * 1000, expireTime.getTime());
  }

  @Test
  void testGetClaimFromToken() {
    String token = jwtService.generateToken(user, password);

    Date expireTime = jwtService.getClaimFromToken(token, JWTClaimsSet::getExpirationTime);
    assertNotNull(expireTime);
    assertTrue(expireTime.after(new Date()));
  }

  @Test
  void testDoGenerateToken() {
    Date now = new Date(new Date().getTime() / 1000 * 1000);
    when(clockService.nowDate()).thenReturn(now);

    Date dateValue = new Date();
    String stringValue = "stringValue";
    double numberValue = 10.5D;
    long timestamp = new Date().getTime();

    Map<String, Object> customClaims = new HashMap<>();
    customClaims.put("stringClaim", stringValue);
    customClaims.put("numberClaim", numberValue);
    customClaims.put("dateClaim", dateValue);
    customClaims.put("timestampClaim", timestamp);

    byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);

    String token = jwtService.doGenerateToken(customClaims, username, passwordBytes);
    assertNotNull(token);
    assertFalse(token.isEmpty());
    assertTrue(jwtService.isTokenValid(token, user));

    String tokenSubject = jwtService.getClaimFromToken(token, JWTClaimsSet::getSubject);
    assertEquals(username, tokenSubject);

    Date tokenNotBeforeTime = jwtService.getClaimFromToken(token, JWTClaimsSet::getNotBeforeTime);
    assertEquals(now, tokenNotBeforeTime);

    Date tokenIssueTime = jwtService.getClaimFromToken(token, JWTClaimsSet::getIssueTime);
    assertEquals(now, tokenIssueTime);

    Date tokenExpirationTime = jwtService.getClaimFromToken(token, JWTClaimsSet::getExpirationTime);
    assertEquals(now.getTime() + 60 * 60 * 1000, tokenExpirationTime.getTime());

    Object passwordClaim = jwtService.getClaimFromToken(token, jwtClaimsSet -> jwtClaimsSet.getClaim("pss"));
    assertTrue(passwordClaim instanceof String);
    assertEquals(password, passwordClaim);

    Object stringClaim = jwtService.getClaimFromToken(token, jwtClaimsSet -> jwtClaimsSet.getClaim("stringClaim"));
    assertTrue(stringClaim instanceof String);
    assertEquals(stringValue, stringClaim);

    Object numberClaim = jwtService.getClaimFromToken(token, jwtClaimsSet -> jwtClaimsSet.getClaim("numberClaim"));
    assertTrue(numberClaim instanceof Double);
    assertEquals(numberValue, numberClaim);

    Object dateClaim = jwtService.getClaimFromToken(token, jwtClaimsSet -> jwtClaimsSet.getClaim("dateClaim"));
    assertTrue(dateClaim instanceof Long);
    // Dates are stored in seconds in token
    assertEquals(new Date(dateValue.getTime() / 1000 * 1000), new Date((Long) dateClaim * 1000));

    Object timestampClaim = jwtService.getClaimFromToken(token, jwtClaimsSet -> jwtClaimsSet.getClaim("timestampClaim"));
    assertTrue(timestampClaim instanceof Long);
    assertEquals(timestamp, timestampClaim);
  }

}