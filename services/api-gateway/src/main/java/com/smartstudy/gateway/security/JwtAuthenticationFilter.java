package com.smartstudy.gateway.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/users/register",
            "/api/users/login"
    );

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Allow browser CORS preflight requests.
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Login and registration must work without a JWT.
        if (PUBLIC_PATHS.contains(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorizationHeader =
                request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {

            sendUnauthorized(
                    response,
                    "Authorization header is missing or invalid"
            );
            return;
        }

        String token =
                authorizationHeader.substring(7);

        if (token.isBlank()
                || !jwtService.isTokenValid(token)) {

            sendUnauthorized(
                    response,
                    "Invalid or expired token"
            );
            return;
        }

        try {

            Long userId =
                    jwtService.extractUserId(token);

            // Use the authenticated user ID from the JWT.
            request.setAttribute(
                    "authenticatedUserId",
                    userId
            );

            filterChain.doFilter(
                    request,
                    response
            );

        } catch (Exception e) {

            System.err.println(
                    "JWT extraction failed: "
                            + e.getClass().getName()
                            + " - "
                            + e.getMessage()
            );

            sendUnauthorized(
                    response,
                    "Invalid authentication token"
            );
        }
    }

    private void sendUnauthorized(
            HttpServletResponse response,
            String message
    ) throws IOException {

        response.setStatus(
                HttpStatus.UNAUTHORIZED.value()
        );

        response.setContentType(
                "application/json"
        );

        response.getWriter().write(
                "{\"message\":\"" + message + "\"}"
        );
    }
}