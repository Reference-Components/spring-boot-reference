package fi.hiq.reference.spring_boot_reference.security;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.JWEDecryptionKeySelector;
import com.nimbusds.jose.proc.JWEKeySelector;
import com.nimbusds.jose.proc.SimpleSecurityContext;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.JWTProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
    "jwt.secret=EC30895A0CA01E89414AA7D3730DD93C"
})
class JwtEncrypterTest {
  @Resource
  private JwtEncrypter jwtEncrypter;

  private final String jwtSecret = "EC30895A0CA01E89414AA7D3730DD93C";

  @Test
  void testDirectEncrypterSecretIsCorrect() throws KeyLengthException {
    DirectEncrypter directEncrypter = jwtEncrypter.directEncrypter();

    byte[] encodedKey = directEncrypter.getKey().getEncoded();
    assertNotNull(encodedKey);
    // Check that the key is same as the one set in this test's properties
    assertEquals(jwtSecret, new String(encodedKey, StandardCharsets.UTF_8));
  }

  @Test
  void testJwtProcessor() {
    JWTProcessor<SimpleSecurityContext> processor = jwtEncrypter.jwtProcessor();
    assertTrue(processor instanceof ConfigurableJWTProcessor);

    ConfigurableJWTProcessor<SimpleSecurityContext> jwtProcessor =
        (ConfigurableJWTProcessor<SimpleSecurityContext>) processor;

    JWEKeySelector<SimpleSecurityContext> selector = jwtProcessor.getJWEKeySelector();
    assertTrue(selector instanceof JWEDecryptionKeySelector);

    JWEDecryptionKeySelector<SimpleSecurityContext> keySelector =
        (JWEDecryptionKeySelector<SimpleSecurityContext>) selector;

    assertEquals(JWEAlgorithm.DIR, keySelector.getExpectedJWEAlgorithm());
    assertEquals(EncryptionMethod.A128CBC_HS256, keySelector.getExpectedJWEEncryptionMethod());

    JWKSource<SimpleSecurityContext> jwkSource = keySelector.getJWKSource();
    assertTrue(jwkSource instanceof ImmutableSecret);

    ImmutableSecret<SimpleSecurityContext> immutableSecret = (ImmutableSecret<SimpleSecurityContext>) jwkSource;

    assertEquals(jwtSecret, new String(immutableSecret.getSecret(), StandardCharsets.UTF_8));
  }

}