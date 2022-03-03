package fi.hiq.reference.spring_boot_reference.security;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.JWEDecryptionKeySelector;
import com.nimbusds.jose.proc.JWEKeySelector;
import com.nimbusds.jose.proc.SimpleSecurityContext;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;

@Configuration
class JwtEncrypter {
  @Value("${jwt.secret}")
  private String jwtSecret;

  private static final JWEAlgorithm JWE_ALGORITHM = JWEAlgorithm.DIR;
  private static final EncryptionMethod ENCRYPTION_METHOD = EncryptionMethod.A128CBC_HS256;

  static final JWEHeader JWE_HEADER = new JWEHeader(JWE_ALGORITHM, ENCRYPTION_METHOD);

  @Bean
  DirectEncrypter directEncrypter() throws KeyLengthException {
    byte[] secretKey = jwtSecret.getBytes(StandardCharsets.UTF_8);

    return new DirectEncrypter(secretKey);
  }

  @Bean
  JWTProcessor<SimpleSecurityContext> jwtProcessor() {
    ConfigurableJWTProcessor<SimpleSecurityContext> jwtProcessor = new DefaultJWTProcessor<>();

    JWKSource<SimpleSecurityContext> jweKeySource = new ImmutableSecret<>(jwtSecret.getBytes(StandardCharsets.UTF_8));

    JWEKeySelector<SimpleSecurityContext> jweKeySelector = new JWEDecryptionKeySelector<>(JWE_ALGORITHM, ENCRYPTION_METHOD, jweKeySource);

    jwtProcessor.setJWEKeySelector(jweKeySelector);

    return jwtProcessor;
  }

}
