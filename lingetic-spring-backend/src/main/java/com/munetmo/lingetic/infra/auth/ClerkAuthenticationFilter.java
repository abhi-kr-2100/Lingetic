package com.munetmo.lingetic.infra.auth;

import com.clerk.backend_api.helpers.jwks.AuthenticateRequest;
import com.clerk.backend_api.helpers.jwks.AuthenticateRequestOptions;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;

@Component
public class  ClerkAuthenticationFilter extends OncePerRequestFilter {
    @Value("${clerk.apiKey}")
    @Nullable
    private String clerkAPIKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (clerkAPIKey == null) {
            throw new IllegalStateException("Clerk API key is not set");
        }

        if (!request.getHeader("Authorization").startsWith("Bearer ")) {
            onUnauthorized(response);
            return;
        }

        var requestState = AuthenticateRequest.authenticateRequest(
                createHttpRequestForClerk(request),
                AuthenticateRequestOptions
                    .secretKey(clerkAPIKey)
                    .build()
        );

        if (requestState.isSignedOut()) {
            onUnauthorized(response);
            return;
        }

        var authentication = new ClerkAuthentication(requestState.claims());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        chain.doFilter(request, response);
    }

    private void onUnauthorized(HttpServletResponse response) throws IOException {
        SecurityContextHolder.clearContext();
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    private HttpRequest createHttpRequestForClerk(HttpServletRequest servletRequest) {
        var authHeader = servletRequest.getHeader("Authorization");
        var uri = servletRequest.getRequestURL().toString();

        return HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("Authorization", authHeader)
                .build();
    }
}
