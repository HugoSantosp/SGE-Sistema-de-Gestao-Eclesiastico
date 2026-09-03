package com.sg.shared.config;

import com.sg.shared.enums.NivelAcesso;
import com.sg.usuario.Usuario;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UserContextHelper {

    /**
     * Retorna o usuário autenticado da requisição atual.
     */
    public Usuario getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof Usuario)) {
            return null;
        }
        return (Usuario) auth.getPrincipal();
    }

    /**
     * Retorna o nível de acesso do usuário logado.
     */
    public NivelAcesso getCurrentUserNivel() {
        Usuario user = getCurrentUser();
        return user != null ? user.getNivel() : null;
    }

    /**
     * Verifica se o usuário logado é PASTOR_PRESIDENTE.
     */
    public boolean isPresidente() {
        return getCurrentUserNivel() == NivelAcesso.PASTOR_PRESIDENTE;
    }
}
