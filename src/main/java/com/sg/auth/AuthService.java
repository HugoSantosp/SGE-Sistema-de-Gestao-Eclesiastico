package com.sg.auth;

import com.sg.auth.dto.*;
import com.sg.shared.exceptions.BusinessException;
import com.sg.shared.exceptions.ResourceNotFoundException;
import com.sg.shared.config.EmailService;
import com.sg.shared.exceptions.InvalidCredentialsException;
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

    public AuthService(UsuarioRepository usuarioRepository,
                       JwtService jwtService,
                       PasswordEncoder passwordEncoder,
                       PasswordResetTokenRepository tokenRepository,
                       EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        Usuario usuario = usuarioRepository.findByEmailOrDocumento(request.user(), request.user())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.senha(), usuario.getSenha())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(usuario.getEmail(), usuario.getNivel().name());

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
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void esqueciSenha(EsqueciSenhaRequestDTO request) {
        // Verifica se o email existe
        usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException());

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

        return new PerfilResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getDocumento(),
                usuario.getFoto(),
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
            usuario.setFoto(request.foto());
        }

        usuario = usuarioRepository.save(usuario);

        return new PerfilResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getDocumento(),
                usuario.getFoto(),
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
        usuarioRepository.save(usuario);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }
}
