package com.SplitIt.user_service.security;

import com.SplitIt.user_service.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_withValidBearerToken_setsAuthentication() throws Exception {
        JwtAuthenticationFilter filterWithService = new JwtAuthenticationFilter(jwtService);
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(jwtService.extractEmail("valid-token")).thenReturn("jane@example.com");
        when(jwtService.extractUserId("valid-token")).thenReturn(42L);

        filterWithService.doFilterInternal(request, response, filterChain);

        UsernamePasswordAuthenticationToken auth =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo("jane@example.com");
        assertThat(auth.getDetails()).isEqualTo(42L);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_withInvalidToken_leavesContextEmpty() throws Exception {
        JwtAuthenticationFilter filterWithService = new JwtAuthenticationFilter(jwtService);
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
        when(jwtService.extractEmail("invalid-token"))
                .thenThrow(new io.jsonwebtoken.security.SignatureException("bad signature"));

        filterWithService.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_withNoAuthorizationHeader_leavesContextEmptyAndContinuesChain() throws Exception {
        JwtAuthenticationFilter filterWithService = new JwtAuthenticationFilter(jwtService);
        when(request.getHeader("Authorization")).thenReturn(null);

        filterWithService.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}
