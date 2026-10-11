package com.smartstudy.gateway.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private final JwtService jwtService =
            mock(JwtService.class);

    private final JwtAuthenticationFilter filter =
            new JwtAuthenticationFilter(jwtService);

    @Test
    void loginShouldBeAllowedWithoutJwt() throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRequestURI("/api/users/login");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain filterChain =
                mock(FilterChain.class);

        filter.doFilter(
                request,
                response,
                filterChain
        );

        verify(filterChain).doFilter(request, response);

        verifyNoInteractions(jwtService);

        assertEquals(200, response.getStatus());
    }

    @Test
    void registerShouldBeAllowedWithoutJwt() throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRequestURI("/api/users/register");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain filterChain =
                mock(FilterChain.class);

        filter.doFilter(
                request,
                response,
                filterChain
        );

        verify(filterChain).doFilter(request, response);

        verifyNoInteractions(jwtService);

        assertEquals(200, response.getStatus());
    }

    @Test
    void protectedEndpointShouldRejectMissingAuthorizationHeader()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRequestURI("/api/quizzes");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain filterChain =
                mock(FilterChain.class);

        filter.doFilter(
                request,
                response,
                filterChain
        );

        assertEquals(401, response.getStatus());

        verify(filterChain, never())
                .doFilter(request, response);

        verifyNoInteractions(jwtService);
    }

    @Test
    void protectedEndpointShouldRejectInvalidToken()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRequestURI("/api/quizzes");
        request.addHeader(
                "Authorization",
                "Bearer invalid-token"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain filterChain =
                mock(FilterChain.class);

        when(jwtService.isTokenValid("invalid-token"))
                .thenReturn(false);

        filter.doFilter(
                request,
                response,
                filterChain
        );

        assertEquals(401, response.getStatus());

        verify(jwtService)
                .isTokenValid("invalid-token");

        verify(filterChain, never())
                .doFilter(request, response);
    }

    @Test
    void protectedEndpointShouldAcceptValidToken()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRequestURI("/api/quizzes");
        request.addHeader(
                "Authorization",
                "Bearer valid-token"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain filterChain =
                mock(FilterChain.class);

        when(jwtService.isTokenValid("valid-token"))
                .thenReturn(true);

        when(jwtService.extractUserId("valid-token"))
                .thenReturn(25L);

        filter.doFilter(
                request,
                response,
                filterChain
        );

        assertEquals(25L,
                request.getAttribute("authenticatedUserId"));

        verify(jwtService)
                .isTokenValid("valid-token");

        verify(jwtService)
                .extractUserId("valid-token");

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void malformedAuthorizationHeaderShouldBeRejected()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRequestURI("/api/quizzes");
        request.addHeader(
                "Authorization",
                "Basic something"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain filterChain =
                mock(FilterChain.class);

        filter.doFilter(
                request,
                response,
                filterChain
        );

        assertEquals(401, response.getStatus());

        verifyNoInteractions(jwtService);

        verify(filterChain, never())
                .doFilter(request, response);

        assertNull(
                request.getAttribute("authenticatedUserId")
        );
    }
}