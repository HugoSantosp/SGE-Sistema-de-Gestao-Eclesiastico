package com.sg.auth;

import com.sg.auth.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Endpoints de login, perfil, alteração e recuperação de senha")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Realizar login", description = "Autentica o usuário por email ou CPF e retorna um token JWT")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO request) {
        LoginResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/alterar-senha")
    @Operation(summary = "Alterar senha", description = "Altera a senha do usuário autenticado (requer senha atual)")
    public ResponseEntity<Map<String, String>> alterarSenha(
            @RequestBody @Valid AlterarSenhaRequestDTO request,
            Authentication authentication) {
        authService.alterarSenha(request, authentication.getName());
        return ResponseEntity.ok(Map.of("message", "Senha alterada com sucesso"));
    }

    @PostMapping("/esqueci-senha")
    @Operation(summary = "Solicitar redefinição de senha",
               description = "Envia um email com link para redefinir a senha (token expira em 30 minutos)")
    public ResponseEntity<Map<String, String>> esqueciSenha(
            @RequestBody @Valid EsqueciSenhaRequestDTO request) {
        authService.esqueciSenha(request);
        return ResponseEntity.ok(Map.of("message", "Se o email existir, você receberá um link de redefinição"));
    }

    @PostMapping("/redefinir-senha")
    @Operation(summary = "Redefinir senha com token",
               description = "Redefine a senha usando o token recebido por email")
    public ResponseEntity<Map<String, String>> redefinirSenha(
            @RequestBody @Valid RedefinirSenhaRequestDTO request) {
        authService.redefinirSenha(request);
        return ResponseEntity.ok(Map.of("message", "Senha redefinida com sucesso"));
    }

    // ===== Perfil =====

    @GetMapping("/me")
    @Operation(summary = "Meus dados", description = "Retorna os dados do perfil do usuário autenticado")
    public ResponseEntity<PerfilResponseDTO> me(Authentication authentication) {
        return ResponseEntity.ok(authService.getCurrentUserProfile(authentication.getName()));
    }

    @PutMapping("/me")
    @Operation(summary = "Atualizar perfil", description = "Atualiza nome, email e foto do usuário autenticado")
    public ResponseEntity<PerfilResponseDTO> atualizarPerfil(
            @RequestBody @Valid AtualizarPerfilRequestDTO request,
            Authentication authentication) {
        return ResponseEntity.ok(authService.atualizarPerfil(request, authentication.getName()));
    }
}
