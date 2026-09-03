package com.sg.escala.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record EscalaConfirmacaoRequestDTO(
        @NotBlank @Size(max = 100) String nome,
        @Email String email,
        @Size(max = 20) String celular,
        @NotEmpty List<Long> dataIds
) {}
