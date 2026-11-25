package br.com.fiap.restaurant_management.service;

import br.com.fiap.restaurant_management.config.JwtProperties;
import br.com.fiap.restaurant_management.domain.Address;
import br.com.fiap.restaurant_management.domain.User;
import br.com.fiap.restaurant_management.domain.UserType;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import static org.assertj.core.api.Assertions.assertThat;

class TokenServiceTest {

    private final SecretKey key = new SecretKeySpec(new byte[32], "HmacSHA256");
    private final TokenService tokenService = new TokenService(
            new NimbusJwtEncoder(new ImmutableSecret<>(key)),
            new JwtProperties("unused-in-this-test", 60));
    private final JwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key)
            .macAlgorithm(MacAlgorithm.HS256)
            .build();

    @Test
    void shouldGenerateTokenWithUserIdAndClaims() {
        Address address = new Address("Avenida Paulista", "1000", "São Paulo", "01310-100");
        User user = new User("Maria Souza", "maria@example.com", "maria.souza", "hash",
                UserType.CUSTOMER, address);
        ReflectionTestUtils.setField(user, "id", 1L);

        Jwt jwt = decoder.decode(tokenService.generate(user));

        assertThat(jwt.getSubject()).isEqualTo("1");
        assertThat(jwt.getClaimAsString("login")).isEqualTo("maria.souza");
        assertThat(jwt.getClaimAsString("type")).isEqualTo("CUSTOMER");
        assertThat(jwt.getExpiresAt()).isAfter(jwt.getIssuedAt());
        assertThat(jwt.getClaims()).doesNotContainKey("password");
    }

    @Test
    void shouldReturnExpirationInSeconds() {
        assertThat(tokenService.expiresInSeconds()).isEqualTo(3600);
    }
}