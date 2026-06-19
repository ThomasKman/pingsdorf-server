package com.pingsdorf.server.auth;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/** Convenience accessor for the authenticated {@link AuthPrincipal}. */
@Component
public class CurrentUser {

    /** @return the principal of the current request, or throws if unauthenticated. */
    public AuthPrincipal require() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof AuthPrincipal p)) {
            throw new UsernameNotFoundException("No authenticated user on request");
        }
        return p;
    }
}
