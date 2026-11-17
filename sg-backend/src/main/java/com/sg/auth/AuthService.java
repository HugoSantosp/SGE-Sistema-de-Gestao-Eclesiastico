package com.sg.auth;

import com.sg.auth.dto.*;
import com.sg.shared.exceptions.BusinessException;
import com.sg.shared.exceptions.ResourceNotFoundException;
import com.sg.shared.config.EmailService;
import com.sg.shared.exceptions.InvalidCredentialsException;
import com.sg.shared.services.SignedUrlService;
import com.sg.usuario.Usuario;
import com.sg.usuario.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final SignedUrlService signedUrlService;

    public AuthService(UsuarioRepository usuarioRepository,
                       JwtService jwtService,
                       PasswordEncoder passwordEncoder,
                       PasswordResetTokenRepository tokenRepository,
                       EmailService emailService,
                       SignedUrlService signedUrlService) {
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.signedUrlService = signedUrlService;
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        Usuario usuario = usuarioRepository.findByEmailOrDocumento(request.user(), request.user())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.senha(), usuario.getSenha())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(usuario.getEmail(), usuario.getNivel().name(), versaoToken(usuario));

        return new LoginResponseDTO(
                token,
                "Bearer",
                usuario.getNome(),
                usuario.getNivel().name(),
                usuario.getId(),
                usuario.isSenhaTemporaria()
        );
    }

    @Transactional
    public void alterarSenha(AlterarSenhaRequestDTO request, String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException());

        if (!passwordEncoder.matches(request.senhaAtual(), usuario.getSenha())) {
            throw new InvalidCredentialsException();
        }

        usuario.setSenha(passwordEncoder.encode(request.novaSenha()));
        // Limpa a flag de senha temporária após a troca
        if (usuario.isSenhaTemporaria()) {
            usuario.setSenhaTemporaria(false);
        }
        // Invalida tokens emitidos antes da troca de senha
        usuario.setTokenVersion(versaoToken(usuario) + 1);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void esqueciSenha(EsqueciSenhaRequestDTO request) {
        // Resposta idêntica para email existente e inexistente: evita enumeração de usuários
        var usuarioOpt = usuarioRepository.findByEmail(request.email());
        if (usuarioOpt.isEmpty()) {
            return;
        }

        // Remove tokens anteriores para este email
        tokenRepository.deleteByEmail(request.email());

        // Gera novo token com expiração de 30 minutos
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .email(request.email())
                .token(token)
                .expiryDate(LocalDateTime.now().plusMinutes(30))
                .used(false)
                .build();
        tokenRepository.save(resetToken);

        // Envia email com o link de redefinição
        emailService.enviarLinkRedefinirSenha(request.email(), token);
    }

    public PerfilResponseDTO getCurrentUserProfile(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + email));

        // Gera URL assinada para a foto (se existir)
        String fotoUrl = usuario.getFoto() != null && !usuario.getFoto().isBlank()
                ? signedUrlService.generateSignedUrl(usuario.getFoto())
                : null;

        return new PerfilResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getDocumento(),
                fotoUrl,
                usuario.getNivel().name(),
                usuario.getIdPessoa()
        );
    }

    @Transactional
    public PerfilResponseDTO atualizarPerfil(AtualizarPerfilRequestDTO request, String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + email));

        // Verifica se o novo email já está em uso por outro usuário
        if (!request.email().equals(email) && usuarioRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email já está em uso por outro usuário");
        }

        usuario.setNome(request.nome());
        usuario.setEmail(request.email());

        if (request.foto() != null) {
            // String vazia = remoção explícita; null = não alterar a foto atual
            usuario.setFoto(request.foto().isBlank() ? null : request.foto());
        }

        usuario = usuarioRepository.save(usuario);

        // Gera URL assinada para a foto (se existir)
        String fotoUrl = usuario.getFoto() != null && !usuario.getFoto().isBlank()
                ? signedUrlService.generateSignedUrl(usuario.getFoto())
                : null;

        return new PerfilResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getDocumento(),
                fotoUrl,
                usuario.getNivel().name(),
                usuario.getIdPessoa()
        );
    }

    @Transactional
    public void redefinirSenha(RedefinirSenhaRequestDTO request) {
        PasswordResetToken resetToken = tokenRepository.findByToken(request.token())
                .orElseThrow(() -> new InvalidCredentialsException());

        if (resetToken.isUsed() || resetToken.isExpired()) {
            throw new InvalidCredentialsException();
        }

        Usuario usuario = usuarioRepository.findByEmail(resetToken.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException());

        usuario.setSenha(passwordEncoder.encode(request.novaSenha()));
        // Invalida tokens emitidos antes da redefinição
        usuario.setTokenVersion(versaoToken(usuario) + 1);
        usuarioRepository.save(usuario);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }

    /**
     * Logout: incrementa a versão do token do usuário, invalidando todos os JWTs
     * emitidos antes deste momento (todas as sessões/dispositivos).
     */
    @Transactional
    public void logout(String email) {
        usuarioRepository.findByEmail(email).ifPresent(usuario -> {
            usuario.setTokenVersion(versaoToken(usuario) + 1);
            usuarioRepository.save(usuario);
        });
    }

    /** Versão atual do token, tratando null como 0 (registros antigos). */
    private long versaoToken(Usuario usuario) {
        return usuario.getTokenVersion() == null ? 0L : usuario.getTokenVersion();
    }
}
