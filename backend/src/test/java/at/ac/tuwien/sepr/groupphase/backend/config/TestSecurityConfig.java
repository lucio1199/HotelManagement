package at.ac.tuwien.sepr.groupphase.backend.config;

import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtAuthorizationFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;


import java.io.IOException;
import java.util.List;

import static at.ac.tuwien.sepr.groupphase.backend.basetest.TestData.ADMIN_USER;

@TestConfiguration
public class TestSecurityConfig {

    @Bean(name = "securityPropertiesStub")
    @Primary
    public SecurityProperties securityPropertiesStub() {
        SecurityProperties securityProperties = Mockito.mock(SecurityProperties.class);

        Mockito.when(securityProperties.getAuthHeader()).thenReturn("Authorization");
        Mockito.when(securityProperties.getAuthTokenPrefix()).thenReturn("Bearer ");
        Mockito.when(securityProperties.getJwtSecret()).thenReturn("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!@");
        Mockito.when(securityProperties.getJwtIssuer()).thenReturn("test-issuer");
        Mockito.when(securityProperties.getJwtAudience()).thenReturn("test-audience");
        Mockito.when(securityProperties.getJwtExpirationTime()).thenReturn(3600L);
        return securityProperties;
    }

    @Bean(name = "userDetailsServiceStub")
    @Primary
    public UserDetailsService userDetailsServiceStub() {
        UserDetailsService mockService = Mockito.mock(UserDetailsService.class);
        UserDetails adminUserDetails = User.builder()
            .username(ADMIN_USER)
            .password("password")
            .authorities("ROLE_ADMIN", "ROLE_GUEST")
            .build();

        Mockito.when(mockService.loadUserByUsername(ADMIN_USER))
            .thenReturn(adminUserDetails);

        return mockService;
    }
}
