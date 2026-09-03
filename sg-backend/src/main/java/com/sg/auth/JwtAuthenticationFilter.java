package com.sg.auth;

import com.sg.usuario.Usuario;
import com.sg.usuario.UsuarioService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Set<String> PERMITIDAS_SENHA_TEMP = Set.of(
            "/auth/alterar-senha",
            "/auth/me",
            "/auth/logout",
            "/api/upload"
    );

    private final JwtService jwtService;
    private final UsuarioService usuarioService;

    public JwtAuthenticationFilter(JwtService jwtService, UsuarioService usuarioService) {
        this.jwtService = jwtService;
        this.usuarioService = usuarioService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        String path = request.getRequestURI();

        try {
            if (jwtService.isValid(token)) {
                String email = jwtService.extractEmail(token);

                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    var userDetails = usuarioService.loadUserByUsername(email);

                    // Usuário com senha temporária só pode acessar rotas permitidas
                    if (userDetails instanceof Usuario usuario && usuario.isSenhaTemporaria()) {
                        if (!isRotaPermitida(path)) {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.getWriter().write(
                                "{\"status\":403,\"error\":\"Você precisa alterar sua senha antes de acessar o sistema. Acesse /auth/alterar-senha.\",\"redirect\":\"/perfil\"}"
                            );
                            return;
                        }
                    }

                    var authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            logger.debug("Token JWT inválido: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private boolean isRotaPermitida(String path) {
        for (String rota : PERMITIDAS_SENHA_TEMP) {
            if (path.startsWith(rota)) {
                return true;
            }
        }
        return false;
    }
}
