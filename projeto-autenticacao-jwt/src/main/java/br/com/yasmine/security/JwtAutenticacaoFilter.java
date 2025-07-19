package br.com.yasmine.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAutenticacaoFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    public JwtAutenticacaoFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        if ("/auth".equals(path) || "/actuator/health".equals(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtService.validateToken(token)) {
                String nomeUsuario = jwtService.extractUsername(token);

                List<SimpleGrantedAuthority> autorizacao;
                if ("admin".equals(nomeUsuario)) {
                    autorizacao = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
                } else {
                    autorizacao = List.of(new SimpleGrantedAuthority("ROLE_USER"));
                }

                AbstractAuthenticationToken auth = new AbstractAuthenticationToken(autorizacao) {
                    @Override
                    public Object getCredentials() {
                        return null;
                    }
                    @Override
                    public Object getPrincipal() {
                        return nomeUsuario;
                    }
                };
                auth.setAuthenticated(true);

                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        filterChain.doFilter(request, response);
    }
}
