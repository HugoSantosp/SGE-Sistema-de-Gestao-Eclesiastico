package com.sg.usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sg.shared.enums.NivelAcesso;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @Query("SELECT u FROM Usuario u WHERE u.email = :email OR u.documento = :documento")
    Optional<Usuario> findByEmailOrDocumento(String email, String documento);

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByDocumento(String documento);

    boolean existsByEmail(String email);

    List<Usuario> findByNivelIn(List<NivelAcesso> niveis);
}
