package br.com.yasmine.security;

import br.com.yasmine.dto.autenticacao.RoleEnum;
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
    private final AutenticacaoService autService;

    public JwtAutenticacaoFilter(JwtService jwtService, AutenticacaoService autService) {
        this.jwtService = jwtService;
        this.autService = autService;
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
                String role = jwtService.extractRole(token);

                RoleEnum roleEnum = autService.validaRole(role);
                if (roleEnum == null) {
                    // Papel inválido → rejeita autenticação
                    return;
                }

                List<SimpleGrantedAuthority> autorizacao;
                if ("admin".equalsIgnoreCase(role)) {
                    autorizacao = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
                } else {
                    autorizacao = List.of(new SimpleGrantedAuthority("ROLE_USER"));
                }

                if (SecurityContextHolder.getContext().getAuthentication() == null) {
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
        }
        filterChain.doFilter(request, response);
    }
}
