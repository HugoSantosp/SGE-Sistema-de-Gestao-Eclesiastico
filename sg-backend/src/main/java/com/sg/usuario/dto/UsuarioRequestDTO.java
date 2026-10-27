package com.sg.usuario.dto;

import com.sg.shared.enums.NivelAcesso;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioRequestDTO(
        @NotBlank(message = "Nome é obrigatório")
        String nome,

        String documento,

        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        String email,

        @Size(min = 3, message = "Senha deve ter no mínimo 3 caracteres (deixe vazio para manter)")
        String senha,

        @NotNull(message = "Nível de acesso é obrigatório")
        NivelAcesso nivel,

        Long idPessoa,

        String foto
) {}
