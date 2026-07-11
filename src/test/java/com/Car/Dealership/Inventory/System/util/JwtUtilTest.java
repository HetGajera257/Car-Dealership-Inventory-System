package com.Car.Dealership.Inventory.System.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    private static final String SECRET =
            "YourSuperSecretKeyThatIsAtLeast256BitsLongForHS256Algorithm123456";
    private static final long EXPIRATION = 86400000L; // 24 hours

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", EXPIRATION);

        userDetails = new User(
                "het@example.com",
                "hashedPassword",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    // ─── Token Generation ────────────────────────────────────────────────────

    @Test
    @DisplayName("generateToken: returns a non-null, non-empty JWT string")
    void generateToken_returnsNonEmptyJwt() {
        String token = jwtUtil.generateToken(userDetails);

        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("generateToken: JWT has 3 parts separated by dots")
    void generateToken_hasThreeParts() {
        String token = jwtUtil.generateToken(userDetails);
        String[] parts = token.split("\\.");

        assertThat(parts).hasSize(3);
    }

    // ─── Username Extraction ──────────────────────────────────────────────────

    @Test
    @DisplayName("extractUsername: returns the email used to generate the token")
    void extractUsername_returnsCorrectEmail() {
        String token = jwtUtil.generateToken(userDetails);

        String extracted = jwtUtil.extractUsername(token);

        assertThat(extracted).isEqualTo("het@example.com");
    }

    // ─── Expiration Extraction ────────────────────────────────────────────────

    @Test
    @DisplayName("extractExpiration: returns a future date for a fresh token")
    void extractExpiration_returnsFutureDate() {
        String token = jwtUtil.generateToken(userDetails);

        Date expiration = jwtUtil.extractExpiration(token);

        assertThat(expiration).isAfter(new Date());
    }

    // ─── Token Validation ────────────────────────────────────────────────────

    @Test
    @DisplayName("validateToken: returns true for a valid token and matching user")
    void validateToken_validTokenAndUser_returnsTrue() {
        String token = jwtUtil.generateToken(userDetails);

        boolean valid = jwtUtil.validateToken(token, userDetails);

        assertThat(valid).isTrue();
    }

    @Test
    @DisplayName("validateToken: returns false when token belongs to a different user")
    void validateToken_differentUser_returnsFalse() {
        String token = jwtUtil.generateToken(userDetails);

        UserDetails otherUser = new User(
                "other@example.com",
                "hashedPassword",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        boolean valid = jwtUtil.validateToken(token, otherUser);

        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("validateToken: returns false for an expired token")
    void validateToken_expiredToken_returnsFalse() {
        // Set expiration to -1ms so the token is immediately expired
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1L);
        String expiredToken = jwtUtil.generateToken(userDetails);

        // Reset expiration to normal so validation proceeds
        ReflectionTestUtils.setField(jwtUtil, "expiration", EXPIRATION);

        boolean valid = jwtUtil.validateToken(expiredToken, userDetails);

        assertThat(valid).isFalse();
    }

    // ─── Invalid Token Handling ───────────────────────────────────────────────

    @Test
    @DisplayName("extractUsername: throws exception for a malformed token")
    void extractUsername_malformedToken_throwsException() {
        assertThatThrownBy(() -> jwtUtil.extractUsername("this.is.not.a.jwt"))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("extractUsername: throws exception for a tampered token")
    void extractUsername_tamperedToken_throwsException() {
        String token = jwtUtil.generateToken(userDetails);
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";

        assertThatThrownBy(() -> jwtUtil.extractUsername(tampered))
                .isInstanceOf(Exception.class);
    }

    // ─── Two tokens are unique ────────────────────────────────────────────────

    @Test
    @DisplayName("generateToken: two calls produce different tokens (issuedAt differs)")
    void generateToken_calledTwice_producesDifferentTokens() throws InterruptedException {
        String token1 = jwtUtil.generateToken(userDetails);
        Thread.sleep(1000); // ensure different issuedAt timestamp
        String token2 = jwtUtil.generateToken(userDetails);

        assertThat(token1).isNotEqualTo(token2);
    }
}
