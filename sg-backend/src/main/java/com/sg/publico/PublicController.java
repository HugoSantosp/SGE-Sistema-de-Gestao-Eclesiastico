package com.sg.publico;

import com.sg.shared.enums.NivelAcesso;
import com.sg.config.Configuracao;
import com.sg.config.ConfiguracaoRepository;
import com.sg.usuario.Usuario;
import com.sg.usuario.UsuarioRepository;
import com.sg.bispos.Bispo;
import com.sg.bispos.BispoRepository;
import com.sg.presbiteros.Presbitero;
import com.sg.presbiteros.PresbiteroRepository;
import com.sg.eventos.EventoService;
import com.sg.celulas.CelulaService;
import com.sg.profissionais.ProfissionalService;
import com.sg.ministerios.MinisterioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public")
@Tag(name = "Público", description = "Endpoints públicos (sem autenticação)")
public class PublicController {

    private final ConfiguracaoRepository configuracaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final BispoRepository bispoRepository;
    private final PresbiteroRepository presbiteroRepository;
    private final EventoService eventoService;
    private final CelulaService celulaService;
    private final ProfissionalService profissionalService;
    private final MinisterioService ministerioService;

    public PublicController(ConfiguracaoRepository configuracaoRepository, UsuarioRepository usuarioRepository,
                            BispoRepository bispoRepository, PresbiteroRepository presbiteroRepository,
                            EventoService eventoService, CelulaService celulaService,
                            ProfissionalService profissionalService, MinisterioService ministerioService) {
        this.configuracaoRepository = configuracaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.bispoRepository = bispoRepository;
        this.presbiteroRepository = presbiteroRepository;
        this.eventoService = eventoService;
        this.celulaService = celulaService;
        this.profissionalService = profissionalService;
        this.ministerioService = ministerioService;
    }

    @GetMapping("/info")
    @Operation(summary = "Retorna informações públicas da igreja (configuradas em Configurações)")
    public ResponseEntity<?> info() {
        return ResponseEntity.ok(Map.of(
                "nome", configValor("nome_igreja", "ICERT - Agência do Reino de Deus"),
                "endereco", configValor("endereco_igreja", "São João de Meriti, RJ"),
                "telefone", configValor("telefone_igreja", ""),
                "email", configValor("email_igreja", "contato@icertag.com.br"),
                "horarios", List.of(
                        Map.of("dia", "Domingo", "horario", "09:00"),
                        Map.of("dia", "Quarta-feira", "horario", "19:30")
                )
        ));
    }

    private String configValor(String nome, String fallback) {
        return configuracaoRepository.findByNome(nome)
                .map(Configuracao::getValor)
                .filter(v -> v != null && !v.isBlank())
                .orElse(fallback);
    }

    @GetMapping("/pastores")
    @Operation(summary = "Retorna lista de liderança (pastores, bispos e presbíteros)")
    public ResponseEntity<List<Map<String, Object>>> pastores() {
        var result = new ArrayList<Map<String, Object>>();

        // 1. Busca pastores (usuários com nivel PASTOR)
        List<Usuario> pastores = usuarioRepository.findByNivelIn(
                List.of(NivelAcesso.PASTOR_PRESIDENTE, NivelAcesso.PASTOR_AUXILIAR)
        );
        pastores.stream()
                .filter(u -> !"admin@sge.com".equals(u.getEmail()))
                .forEach(u -> result.add(Map.of(
                        "nome", u.getNome(),
                        "email", u.getEmail() != null ? u.getEmail() : "",
                        "foto", u.getFoto() != null ? u.getFoto() : "",
                        "cargo", u.getNivel() == NivelAcesso.PASTOR_PRESIDENTE ? "Pastor Presidente" : "Pastor Auxiliar"
                )));

        // 2. Busca bispos
        bispoRepository.findAll().forEach(b -> result.add(Map.of(
                "nome", b.getNome(),
                "email", b.getEmail() != null ? b.getEmail() : "",
                "foto", b.getFoto() != null ? b.getFoto() : "",
                "cargo", "Bispo"
        )));

        // 3. Busca presbíteros
        presbiteroRepository.findAll().forEach(p -> result.add(Map.of(
                "nome", p.getNome(),
                "email", p.getEmail() != null ? p.getEmail() : "",
                "foto", p.getFoto() != null ? p.getFoto() : "",
                "cargo", "Presbítero"
        )));

        return ResponseEntity.ok(result);
    }

    @GetMapping("/eventos")
    @Operation(summary = "Retorna eventos (query: ?mes=&ano= opcionais)")
    public ResponseEntity<List<Map<String, Object>>> eventos(
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer ano) {
        var eventos = (mes != null && ano != null)
                ? eventoService.listarPorMesAno(mes, ano)
                : eventoService.listarProximos();
        var result = eventos.stream().map(e -> Map.<String, Object>of(
                "id", e.getId(), "titulo", e.getTitulo(),
                "descricao", e.getDescricao() != null ? e.getDescricao() : "",
                "data", e.getData().toString(),
                "hora", e.getHora() != null ? e.getHora().toString() : "",
                "local", e.getLocal() != null ? e.getLocal() : ""
        )).toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/celulas")
    @Operation(summary = "Retorna lista de células/grupos pequenos")
    public ResponseEntity<List<Map<String, Object>>> celulas() {
        var result = celulaService.listarPublicas().stream().map(c -> Map.<String, Object>of(
                "id", c.getId(), "nome", c.getNome(),
                "lider", c.getLider() != null ? c.getLider() : "",
                "endereco", c.getEndereco() != null ? c.getEndereco() : "",
                "diaSemana", c.getDiaSemana() != null ? c.getDiaSemana() : "",
                "horario", c.getHorario() != null ? c.getHorario().toString() : "",
                "descricao", c.getDescricao() != null ? c.getDescricao() : "",
                "foto", c.getFoto() != null ? c.getFoto() : ""
        )).toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/profissionais")
    @Operation(summary = "Retorna lista de profissionais da igreja (mural)")
    public ResponseEntity<List<Map<String, Object>>> profissionais() {
        var result = profissionalService.listarPublicos().stream().map(p -> Map.<String, Object>of(
                "id", p.getId(), "nome", p.getNome(),
                "especialidade", p.getEspecialidade() != null ? p.getEspecialidade() : "",
                "telefone", p.getTelefone() != null ? p.getTelefone() : "",
                "email", p.getEmail() != null ? p.getEmail() : "",
                "foto", p.getFoto() != null ? p.getFoto() : "",
                "descricao", p.getDescricao() != null ? p.getDescricao() : ""
        )).toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/ministerios")
    @Operation(summary = "Retorna lista de ministérios")
    public ResponseEntity<List<Map<String, Object>>> ministerios() {
        var result = ministerioService.listarTodos().stream().map(m -> Map.<String, Object>of(
                "id", m.getId(), "nome", m.getNome(),
                "descricao", m.getDescricao() != null ? m.getDescricao() : "",
                "foto", m.getFoto() != null ? m.getFoto() : ""
        )).toList();
        return ResponseEntity.ok(result);
    }
}
