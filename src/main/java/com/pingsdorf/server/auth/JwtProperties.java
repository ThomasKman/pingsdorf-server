package com.pingsdorf.server.auth;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for JWT issuance.
 *
 * @param secret  HMAC-SHA256 secret. Must be at least 32 bytes.
 * @param issuer  Issuer claim ({@code iss}).
 * @param ttl     How long issued tokens remain valid.
 */
@ConfigurationProperties(prefix = "pingsdorf.jwt")
public record JwtProperties(
        String secret,
        String issuer,
        Duration ttl
) {
}
