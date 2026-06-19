package com.pingsdorf.server.auth;

/**
 * Authenticated principal stored on the security context.
 * Backed by JWT claims (userId, householdId).
 */
public record AuthPrincipal(String userId, String householdId) {
}
