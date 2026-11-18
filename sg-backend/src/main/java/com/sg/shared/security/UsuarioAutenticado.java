package com.sg.shared.security;

import com.sg.usuario.Usuario;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Identifica o usuário responsável pela operação a partir do token já validado
 * pelo JwtAuthenticationFilter. Nunca confia em id enviado pelo cliente.
 */
public final class UsuarioAutenticado {

    private UsuarioAutenticado() {
    }

    /** Retorna null quando não há usuário autenticado (ex.: teste unitário sem contexto). */
    public static Long id() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Usuario usuario) {
            return usuario.getId();
        }
        return null;
    }
}
