package com.airline.auth.security;
import java.io.IOException;
import java.io.InputStream;
import java.security.interfaces.RSAPublicKey;
import java.security.interfaces.RSAPrivateKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;

@Configuration
public class JwtConfig {
  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  RSAPublicKey publicKey(@Value("${jwt.public-key}") Resource resource) throws IOException {
    try (InputStream input = resource.getInputStream()) {
      return RsaKeyConverters.x509().convert(input);
    }
  }

  @Bean
  JwtDecoder jwtDecoder(RSAPublicKey publicKey) {
    return NimbusJwtDecoder.withPublicKey(publicKey).build();
  }

  @Bean
  JwtEncoder jwtEncoder(
      RSAPublicKey publicKey, @Value("${jwt.private-key}") Resource privateKeyResource)
      throws IOException {
    RSAPrivateKey privateKey;
    try (InputStream input = privateKeyResource.getInputStream()) {
      privateKey = RsaKeyConverters.pkcs8().convert(input);
    }

    RSAKey rsaKey = new RSAKey.Builder(publicKey).privateKey(privateKey).build();
    JWKSet keySet = new JWKSet(rsaKey);
    return new NimbusJwtEncoder(new ImmutableJWKSet<SecurityContext>(keySet));
  }
}
