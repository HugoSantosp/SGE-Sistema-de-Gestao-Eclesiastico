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

    /** Usuário vinculado a um membro da igreja (idPessoa). */
    Optional<Usuario> findFirstByIdPessoa(Long idPessoa);

    /** Usuários cujo nome contém o trecho informado (usado para resolver fotos por nome). */
    List<Usuario> findByNomeContainingIgnoreCase(String nome);

    /** Todos os usuários com o documento informado (o documento pode estar duplicado). */
    List<Usuario> findByDocumentoIgnoreCase(String documento);

    boolean existsByEmail(String email);

    List<Usuario> findByNivelIn(List<NivelAcesso> niveis);
}
