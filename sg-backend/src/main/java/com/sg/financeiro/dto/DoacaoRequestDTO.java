package com.sg.financeiro.dto;

import com.sg.shared.enums.FormaPagamento;
import com.sg.shared.enums.TipoDoacao;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Dízimo e Primícia exigem membroId; Oferta exige nomeDoador.
 * A validação condicional fica no service, que devolve 422 com mensagem clara.
 */
public record DoacaoRequestDTO(
        @NotNull TipoDoacao tipo,
        Long membroId,
        @Size(max = 150) String nomeDoador,
        @NotNull LocalDate dataDoacao,
        @NotNull FormaPagamento formaPagamento,
        @NotNull @DecimalMin(value = "0.01") BigDecimal valor,
        @Size(max = 255) String descricao,
        Long categoriaId
) {}
