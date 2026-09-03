package com.sg.usuario;

import com.sg.bispos.Bispo;
import com.sg.bispos.BispoRepository;
import com.sg.membros.Membro;
import com.sg.membros.MembroRepository;
import com.sg.ministerios.MinisterioMembroRepository;
import com.sg.presbiteros.Presbitero;
import com.sg.presbiteros.PresbiteroRepository;
import com.sg.secretarios.Secretario;
import com.sg.secretarios.SecretarioRepository;
import com.sg.shared.enums.NivelAcesso;
import com.sg.shared.enums.StatusMembro;
import com.sg.shared.exceptions.ResourceNotFoundException;
import com.sg.tesoureiros.Tesoureiro;
import com.sg.tesoureiros.TesoureiroRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TesoureiroRepository tesoureiroRepository;
    private final PresbiteroRepository presbiteroRepository;
    private final SecretarioRepository secretarioRepository;
    private final BispoRepository bispoRepository;
    private final MembroRepository membroRepository;
    private final MinisterioMembroRepository ministerioMembroRepository;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          TesoureiroRepository tesoureiroRepository,
                          PresbiteroRepository presbiteroRepository,
                          SecretarioRepository secretarioRepository,
                          BispoRepository bispoRepository,
                          MembroRepository membroRepository,
                          MinisterioMembroRepository ministerioMembroRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tesoureiroRepository = tesoureiroRepository;
        this.presbiteroRepository = presbiteroRepository;
        this.secretarioRepository = secretarioRepository;
        this.bispoRepository = bispoRepository;
        this.membroRepository = membroRepository;
        this.ministerioMembroRepository = ministerioMembroRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", id));
    }

    private static final String SENHA_TEMPORARIA = "12345678";

    @Transactional
    public Usuario salvar(Usuario usuario) {
        // Detecta se é uma atualização (já tem ID no banco)
        boolean isNew = usuario.getId() == null;
        NivelAcesso nivelAnterior = null;
        if (!isNew) {
            nivelAnterior = usuarioRepository.findById(usuario.getId())
                    .map(Usuario::getNivel)
                    .orElse(null);
        }

        if (isNew) {
            // Novo usuário: sempre define senha temporária
            usuario.setSenha(passwordEncoder.encode(SENHA_TEMPORARIA));
            usuario.setSenhaTemporaria(true);
        } else if (usuario.getSenha() != null && !usuario.getSenha().isEmpty()
                && !usuario.getSenha().startsWith("$2a$") && !usuario.getSenha().startsWith("$2b$")) {
            // Atualização: codifica a senha se foi alterada (senha em texto puro)
            usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        }

        Usuario saved = usuarioRepository.save(usuario);

        // Sincroniza com a tabela de cargo correspondente
        if (nivelAnterior != null && nivelAnterior != usuario.getNivel()) {
            // Role mudou — remove o registro do cargo anterior
            removerCargo(saved, nivelAnterior);
        }
        criarOuAtualizarCargo(saved);

        return saved;
    }

    @Transactional
    public Usuario resetarSenha(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", id));

        usuario.setSenha(passwordEncoder.encode(SENHA_TEMPORARIA));
        usuario.setSenhaTemporaria(true);

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void deletar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", id));

        // Remove o registro de cargo correspondente antes de deletar
        removerCargo(usuario, usuario.getNivel());

        usuarioRepository.deleteById(id);
    }

    // ===== Métodos privados de sincronização =====

    private void criarOuAtualizarCargo(Usuario u) {
        Long pessoaId = null;

        switch (u.getNivel()) {
            case TESOUREIRO -> {
                Tesoureiro t = tesoureiroRepository.findByEmail(u.getEmail())
                        .orElse(new Tesoureiro());
                t.setNome(u.getNome());
                t.setEmail(u.getEmail());
                t.setDocumento(u.getDocumento());
                if (t.getId() == null) t.setDataCad(LocalDate.now());
                t = tesoureiroRepository.save(t);
                pessoaId = t.getId();
            }
            case PASTOR_AUXILIAR -> {
                Presbitero p = presbiteroRepository.findByEmail(u.getEmail())
                        .orElse(new Presbitero());
                p.setNome(u.getNome());
                p.setEmail(u.getEmail());
                p.setDocumento(u.getDocumento());
                if (p.getId() == null) p.setDataCad(LocalDate.now());
                p = presbiteroRepository.save(p);
                pessoaId = p.getId();
            }
            case SECRETARIO -> {
                Secretario s = secretarioRepository.findByEmail(u.getEmail())
                        .orElse(new Secretario());
                s.setNome(u.getNome());
                s.setEmail(u.getEmail());
                s.setDocumento(u.getDocumento());
                if (s.getId() == null) s.setDataCad(LocalDate.now());
                s = secretarioRepository.save(s);
                pessoaId = s.getId();
            }
            case PASTOR_PRESIDENTE -> {
                Bispo b = bispoRepository.findByEmail(u.getEmail())
                        .orElse(new Bispo());
                b.setNome(u.getNome());
                b.setEmail(u.getEmail());
                b.setDocumento(u.getDocumento());
                if (b.getId() == null) b.setDataCad(LocalDate.now());
                b = bispoRepository.save(b);
                pessoaId = b.getId();
            }
            case MEMBRO -> {
                // Usuário MEMBRO é sincronizado com a tabela de membros (vínculo por documento)
                Membro m = membroRepository.findByDocumento(u.getDocumento())
                        .orElse(Membro.builder()
                                .nome(u.getNome())
                                .documento(u.getDocumento())
                                .situacao(StatusMembro.ATIVO)
                                .dataCad(LocalDate.now())
                                .build());
                m.setNome(u.getNome());
                m.setDocumento(u.getDocumento());
                if (m.getId() == null) m.setDataCad(LocalDate.now());
                m = membroRepository.save(m);
                pessoaId = m.getId();
            }
        }

        // Vincula o idPessoa do Usuário ao registro do cargo criado
        if (pessoaId != null && !pessoaId.equals(u.getIdPessoa())) {
            u.setIdPessoa(pessoaId);
            usuarioRepository.save(u);
        }
    }

    private void removerCargo(Usuario usuario, NivelAcesso nivel) {
        switch (nivel) {
            case TESOUREIRO ->
                tesoureiroRepository.findByEmail(usuario.getEmail()).ifPresent(tesoureiroRepository::delete);
            case PASTOR_AUXILIAR ->
                presbiteroRepository.findByEmail(usuario.getEmail()).ifPresent(presbiteroRepository::delete);
            case SECRETARIO ->
                secretarioRepository.findByEmail(usuario.getEmail()).ifPresent(secretarioRepository::delete);
            case PASTOR_PRESIDENTE ->
                bispoRepository.findByEmail(usuario.getEmail()).ifPresent(bispoRepository::delete);
            case MEMBRO -> {
                // Não apaga o Membro (ele tem histórico próprio: escalas, confirmações);
                // apenas remove os vínculos com ministérios.
                membroRepository.findByDocumento(usuario.getDocumento())
                        .ifPresent(m -> ministerioMembroRepository.deleteByMembroId(m.getId()));
            }
        }
    }
}
