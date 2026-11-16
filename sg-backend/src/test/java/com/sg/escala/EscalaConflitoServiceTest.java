package com.sg.escala;

import com.sg.escala.dto.EscalaConflitoDTO;
import com.sg.membros.Membro;
import com.sg.ministerios.Ministerio;
import com.sg.ministerios.MinisterioRepository;
import com.sg.shared.exceptions.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Regra de conflito de disponibilidade: o mesmo membro não pode confirmar a mesma
 * data e o mesmo horário em dois ministérios diferentes.
 *
 * <p>Decisões cobertas aqui: conflito por data + horário; escalas genéricas
 * ({@code ministerioId == null}) ficam fora; escalas do mesmo ministério não conflitam.
 */
@ExtendWith(MockitoExtension.class)
class EscalaConflitoServiceTest {

    private static final Long MEMBRO_ID = 10L;
    private static final Long ESCALA_LOUVOR_ID = 1L;
    private static final Long MINISTERIO_LOUVOR_ID = 1L;
    private static final Long MINISTERIO_MIDIA_ID = 2L;
    private static final LocalDate DIA = LocalDate.of(2026, 11, 22);

    @Mock
    private EscalaConfirmacaoRepository confirmacaoRepository;

    @Mock
    private MinisterioRepository ministerioRepository;

    @InjectMocks
    private EscalaConflitoService service;

    // ===== Cenários =====

    @Test
    @DisplayName("Mesma data e mesmo horário em outro ministério gera conflito")
    void mesmaDataMesmoHorarioConflita() {
        Escala louvor = escala(ESCALA_LOUVOR_ID, "Louvor 22/11", MINISTERIO_LOUVOR_ID);
        EscalaData dataLouvor = data(100L, louvor, DIA, LocalTime.of(10, 0));
        EscalaConfirmacao outra = confirmacaoEm(
                escala(9L, "Mídia 22/11", MINISTERIO_MIDIA_ID),
                data(900L, escala(9L, "Mídia 22/11", MINISTERIO_MIDIA_ID), DIA, LocalTime.of(10, 0)));

        when(confirmacaoRepository.buscarConfirmacoesEmOutrasEscalas(MEMBRO_ID, ESCALA_LOUVOR_ID))
                .thenReturn(List.of(outra));
        when(ministerioRepository.findById(MINISTERIO_MIDIA_ID))
                .thenReturn(Optional.of(Ministerio.builder().id(MINISTERIO_MIDIA_ID).nome("Mídia").build()));

        List<EscalaConflitoDTO> conflitos = service.conflitos(MEMBRO_ID, louvor, List.of(dataLouvor));

        assertThat(conflitos).hasSize(1);
        assertThat(conflitos.get(0).ministerioNome()).isEqualTo("Mídia");
        assertThat(conflitos.get(0).data()).isEqualTo(DIA);
        assertThat(conflitos.get(0).horario()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("Mesma data em horário diferente não gera conflito")
    void mesmaDataHorarioDiferenteNaoConflita() {
        Escala louvor = escala(ESCALA_LOUVOR_ID, "Louvor 22/11", MINISTERIO_LOUVOR_ID);
        EscalaData dataLouvor = data(100L, louvor, DIA, LocalTime.of(10, 0));
        Escala midia = escala(9L, "Mídia 22/11", MINISTERIO_MIDIA_ID);
        EscalaConfirmacao outra = confirmacaoEm(midia, data(900L, midia, DIA, LocalTime.of(19, 0)));

        when(confirmacaoRepository.buscarConfirmacoesEmOutrasEscalas(MEMBRO_ID, ESCALA_LOUVOR_ID))
                .thenReturn(List.of(outra));

        assertThat(service.conflitos(MEMBRO_ID, louvor, List.of(dataLouvor))).isEmpty();
    }

    @Test
    @DisplayName("Escala genérica (sem ministério) não entra na regra")
    void escalaGenericaNaoConflita() {
        Escala generica = escala(ESCALA_LOUVOR_ID, "Culto geral", null);
        EscalaData dataGenerica = data(100L, generica, DIA, LocalTime.of(10, 0));

        assertThat(service.conflitos(MEMBRO_ID, generica, List.of(dataGenerica))).isEmpty();
    }

    @Test
    @DisplayName("Escalas do mesmo ministério não conflitam entre si")
    void mesmoMinisterioNaoConflita() {
        Escala louvor = escala(ESCALA_LOUVOR_ID, "Louvor 22/11", MINISTERIO_LOUVOR_ID);
        EscalaData dataLouvor = data(100L, louvor, DIA, LocalTime.of(10, 0));
        Escala outraLouvor = escala(9L, "Louvor extra", MINISTERIO_LOUVOR_ID);
        EscalaConfirmacao outra = confirmacaoEm(outraLouvor, data(900L, outraLouvor, DIA, LocalTime.of(10, 0)));

        when(confirmacaoRepository.buscarConfirmacoesEmOutrasEscalas(MEMBRO_ID, ESCALA_LOUVOR_ID))
                .thenReturn(List.of(outra));

        assertThat(service.conflitos(MEMBRO_ID, louvor, List.of(dataLouvor))).isEmpty();
    }

    @Test
    @DisplayName("validar lança BusinessException (422) com o ministério na mensagem")
    void validarLancaBusinessException() {
        Escala louvor = escala(ESCALA_LOUVOR_ID, "Louvor 22/11", MINISTERIO_LOUVOR_ID);
        EscalaData dataLouvor = data(100L, louvor, DIA, LocalTime.of(10, 0));
        Escala midia = escala(9L, "Mídia 22/11", MINISTERIO_MIDIA_ID);
        EscalaConfirmacao outra = confirmacaoEm(midia, data(900L, midia, DIA, LocalTime.of(10, 0)));

        when(confirmacaoRepository.buscarConfirmacoesEmOutrasEscalas(MEMBRO_ID, ESCALA_LOUVOR_ID))
                .thenReturn(List.of(outra));
        when(ministerioRepository.findById(MINISTERIO_MIDIA_ID))
                .thenReturn(Optional.of(Ministerio.builder().id(MINISTERIO_MIDIA_ID).nome("Mídia").build()));

        assertThatThrownBy(() -> service.validar(MEMBRO_ID, louvor, List.of(dataLouvor)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("22/11/2026")
                .hasMessageContaining("10:00")
                .hasMessageContaining("Mídia");
    }

    // ===== Builders =====

    private Escala escala(Long id, String titulo, Long ministerioId) {
        return Escala.builder()
                .id(id)
                .titulo(titulo)
                .ministerioId(ministerioId)
                .publicToken("token-" + id)
                .aberta(true)
                .build();
    }

    private EscalaData data(Long id, Escala escala, LocalDate dia, LocalTime horario) {
        return EscalaData.builder()
                .id(id)
                .escala(escala)
                .nomeEvento("Culto")
                .data(dia)
                .horario(horario)
                .local("Templo")
                .build();
    }

    private EscalaConfirmacao confirmacaoEm(Escala escala, EscalaData data) {
        return EscalaConfirmacao.builder()
                .id(500L)
                .escala(escala)
                .membro(Membro.builder().id(MEMBRO_ID).nome("Hugo").build())
                .nome("Hugo")
                .datas(List.of(data))
                .build();
    }
}
