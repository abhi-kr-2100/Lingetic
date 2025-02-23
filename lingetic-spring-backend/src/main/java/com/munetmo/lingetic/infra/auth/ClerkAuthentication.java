package com.munetmo.lingetic.infra.auth;

import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

public class ClerkAuthentication extends AbstractAuthenticationToken {
    private final Object principal;

    public ClerkAuthentication(Object principal) {
        super(AuthorityUtils.NO_AUTHORITIES);
        this.principal = principal;
        super.setAuthenticated(true);
    }

    @Override
    @Nullable
    public Object getCredentials() {
        // No password is involved; short-lived JWT is used for authentication
        return null;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        if (isAuthenticated) {
            throw new IllegalArgumentException(
                "Authentication is managed by Clerk. Cannot set isAuthenticated to true after construction."
            );
        }

        super.setAuthenticated(false);
    }
}
