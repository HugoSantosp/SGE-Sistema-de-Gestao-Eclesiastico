package com.sg.shared.config;

import com.sg.bispos.Bispo;
import com.sg.bispos.BispoRepository;
import com.sg.cargos.Cargo;
import com.sg.cargos.CargoRepository;
import com.sg.celulas.Celula;
import com.sg.celulas.CelulaRepository;
import com.sg.config.Configuracao;
import com.sg.config.ConfiguracaoRepository;
import com.sg.eventos.Evento;
import com.sg.eventos.EventoRepository;
import com.sg.ministerios.Ministerio;
import com.sg.ministerios.MinisterioRepository;
import com.sg.profissionais.Profissional;
import com.sg.profissionais.ProfissionalRepository;
import com.sg.shared.enums.FrequenciaPagamento;
import com.sg.shared.enums.NivelAcesso;
import com.sg.shared.enums.PapelMinisterio;
import com.sg.shared.enums.StatusConta;
import com.sg.shared.enums.StatusMembro;
import com.sg.shared.enums.StatusTarefa;
import com.sg.usuario.Usuario;
import com.sg.usuario.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final ConfiguracaoRepository configuracaoRepository;
    private final MinisterioRepository ministerioRepository;
    private final CelulaRepository celulaRepository;
    private final EventoRepository eventoRepository;
    private final ProfissionalRepository profissionalRepository;
    private final CargoRepository cargoRepository;
    private final BispoRepository bispoRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Senha inicial do admin (bootstrap em produção, via env ADMIN_INITIAL_PASSWORD).
     * Em dev o valor padrão é "123".
     */
    @Value("${app.admin-initial-password:}")
    private String adminInitialPassword;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           ConfiguracaoRepository configuracaoRepository,
                           MinisterioRepository ministerioRepository,
                           CelulaRepository celulaRepository,
                           EventoRepository eventoRepository,
                           ProfissionalRepository profissionalRepository,
                           CargoRepository cargoRepository,
                           BispoRepository bispoRepository,
                           PasswordEncoder passwordEncoder,
                           Environment environment,
                           JdbcTemplate jdbcTemplate) {
        this.usuarioRepository = usuarioRepository;
        this.configuracaoRepository = configuracaoRepository;
        this.ministerioRepository = ministerioRepository;
        this.celulaRepository = celulaRepository;
        this.eventoRepository = eventoRepository;
        this.profissionalRepository = profissionalRepository;
        this.cargoRepository = cargoRepository;
        this.bispoRepository = bispoRepository;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        boolean isProd = environment.acceptsProfiles(Profiles.of("prod"));

        // Corrige constraints de enum desatualizadas antes de qualquer operação
        corrigirConstraintsEnums();

        // Bootstrap do admin: em dev força senha padrão; em prod nunca define senha fixa
        usuarioRepository.findByEmail("admin@sge.com").ifPresent(usuario -> {
            String hashArmazenado = usuario.getSenha();
            // BCrypt hashes têm 60 caracteres; placeholders são mais curtos
            if (hashArmazenado == null || hashArmazenado.length() != 60) {
                if (isProd) {
                    // Produção: usa a senha da env ADMIN_INITIAL_PASSWORD (marcada como temporária),
                    // ou avisa que o bootstrap precisa ser feito manualmente.
                    if (adminInitialPassword != null && !adminInitialPassword.isBlank()) {
                        usuario.setSenha(passwordEncoder.encode(adminInitialPassword));
                        usuario.setSenhaTemporaria(true);
                        usuarioRepository.save(usuario);
                        log.warn("Senha inicial do admin definida via app.admin-initial-password (senha temporária — troca obrigatória no 1º login)");
                    } else {
                        log.warn("Admin 'admin@sge.com' possui senha inválida/placeholder. Defina a env ADMIN_INITIAL_PASSWORD (ou troque a senha manualmente no banco) para habilitar o primeiro acesso.");
                    }
                } else {
                    String senhaDev = (adminInitialPassword != null && !adminInitialPassword.isBlank())
                            ? adminInitialPassword : "123";
                    usuario.setSenha(passwordEncoder.encode(senhaDev));
                    usuarioRepository.save(usuario);
                    log.info("Senha do admin atualizada com hash BCrypt válido (dev)");
                }
            }
        });

        // Garante que as configurações padrão existam
        criarConfigSeNaoExistir("email_super_adm", "admin@sge.com");
        criarConfigSeNaoExistir("nome_igreja", "ICERT - Agência do Reino de Deus");
        criarConfigSeNaoExistir("endereco_igreja", "São João de Meriti, RJ");
        criarConfigSeNaoExistir("telefone_igreja", "(21) 99999-9999");
        criarConfigSeNaoExistir("email_igreja", "contato@icertag.com.br");
        criarConfigSeNaoExistir("qtd_tarefa", "20");

        // Dados essenciais (cargo 'Membro', admin) — criados se ausentes.
        // Necessário sem Flyway: em banco novo essas linhas não existem (antes vinham da migration V2).
        bootstrapDadosIniciais();

        if (isProd) {
            log.info("Ambiente prod: seeds de dados de exemplo (ministérios, células, eventos, profissionais) desabilitados.");
        } else {
            // Seed dados de exemplo para as novas tabelas (apenas dev)
            seedMinisterios();
            seedCelulas();
            seedEventos();
            seedProfissionais();
        }

        log.info("DataInitializer concluído com sucesso");
    }

    /**
     * Cria os dados essenciais caso não existam: cargo 'Membro' e o usuário admin.
     * Substitui o papel da migration V2 em ambientes sem Flyway (ddl-auto: update).
     */
    private void bootstrapDadosIniciais() {
        // Cargo padrão 'Membro'
        if (cargoRepository.count() == 0) {
            cargoRepository.save(Cargo.builder().nome("Membro").build());
            log.info("Bootstrap: cargo 'Membro' criado");
        }

        // Usuário admin — bootstrap do primeiro acesso
        if (usuarioRepository.findByEmail("admin@sge.com").isEmpty()) {
            if (adminInitialPassword != null && !adminInitialPassword.isBlank()) {
                Bispo bispo = bispoRepository.save(Bispo.builder()
                        .nome("Super ADM").email("admin@sge.com").documento("000.000.000-00")
                        .telefone("(21) 99999-9999").endereco("Endereço").foto("logo.png")
                        .dataCad(LocalDate.now()).build());
                usuarioRepository.save(Usuario.builder()
                        .nome("Super ADM").documento("000.000.000-00").email("admin@sge.com")
                        .senha(passwordEncoder.encode(adminInitialPassword))
                        .nivel(NivelAcesso.PASTOR_PRESIDENTE)
                        .idPessoa(bispo.getId()).foto("logo.png")
                        // Em prod: senha temporária (troca obrigatória no 1º login). Em dev: acesso direto.
                        .senhaTemporaria(environment.acceptsProfiles(Profiles.of("prod")))
                        .build());
                log.warn("Bootstrap: admin criado com senha inicial de app.admin-initial-password (troca obrigatória no 1º login)");
            } else {
                log.warn("Nenhum admin encontrado e app.admin-initial-password vazio. Defina a env ADMIN_INITIAL_PASSWORD (ou crie o admin no banco) para o primeiro acesso.");
            }
        }
    }

    /**
     * Corrige constraints CHECK desatualizadas de colunas enum (se existirem).
     * <p>
     * O Hibernate cria constraints `{tabela}_{coluna}_check` quando gera a tabela,
     * numa época em que os enums tinham apenas alguns valores (ex.: NivelAcesso sem
     * MEMBRO, PapelMinisterio sem MUSICO). Como o Flyway está desabilitado e o
     * ddl-auto=update não altera constraints existentes, elas continuariam rejeitando
     * novos valores (SQLState 23514).
     * <p>
     * Aqui recriamos cada constraint com TODOS os valores atuais do enum, mantendo a
     * validação no banco em sincronia com o Java — resolve de vez e acompanha
     * futuros valores adicionados aos enums.
     */
    private void corrigirConstraintsEnums() {
        corrigirConstraintEnum("usuario", "nivel", NivelAcesso.values());
        corrigirConstraintEnum("ministerio_membro", "papel", PapelMinisterio.values());
        corrigirConstraintEnum("membros", "situacao", StatusMembro.values());
        corrigirConstraintEnum("contas_pagar", "status", StatusConta.values());
        corrigirConstraintEnum("contas_pagar", "frequencia", FrequenciaPagamento.values());
        corrigirConstraintEnum("contas_receber", "status", StatusConta.values());
        corrigirConstraintEnum("contas_receber", "frequencia", FrequenciaPagamento.values());
        corrigirConstraintEnum("tarefas", "status_tarefa", StatusTarefa.values());
    }

    /**
     * Atualiza (ou recria) uma constraint CHECK de enum que não inclui todos os valores atuais.
     * A constraint recebe o nome padrão do Hibernate: {tabela}_{coluna}_check.
     */
    private void corrigirConstraintEnum(String tabela, String coluna, Enum<?>[] valores) {
        String constraintName = tabela + "_" + coluna + "_check";
        try {
            List<String> defs = jdbcTemplate.queryForList(
                    "SELECT pg_get_constraintdef(oid) FROM pg_constraint "
                            + "WHERE conname = ? AND conrelid = CAST(? AS regclass)",
                    String.class, constraintName, tabela);
            if (defs.isEmpty()) return; // constraint não existe (banco novo ou já corrigido)

            String def = defs.get(0);
            boolean completa = Arrays.stream(valores)
                    .allMatch(v -> def.contains(v.name()));
            if (completa) return; // já inclui todos os valores atuais

            String lista = Arrays.stream(valores)
                    .map(v -> "'" + v.name() + "'")
                    .collect(Collectors.joining(", "));
            jdbcTemplate.execute("ALTER TABLE " + tabela + " DROP CONSTRAINT " + constraintName);
            jdbcTemplate.execute("ALTER TABLE " + tabela + " ADD CONSTRAINT " + constraintName
                    + " CHECK (" + coluna + " IN (" + lista + "))");
            log.warn("Bootstrap: constraint '{}' atualizada para incluir todos os valores: {}", constraintName, lista);
        } catch (Exception e) {
            log.warn("Bootstrap: não foi possível corrigir a constraint '{}': {}", constraintName, e.getMessage());
        }
    }

    private void criarConfigSeNaoExistir(String nome, String valor) {
        if (configuracaoRepository.findByNome(nome).isEmpty()) {
            configuracaoRepository.save(Configuracao.builder()
                    .nome(nome)
                    .valor(valor)
                    .qtdTarefa(20)
                    .build());
        }
    }

    private void seedMinisterios() {
        if (ministerioRepository.count() > 0) return;
        ministerioRepository.save(Ministerio.builder()
                .nome("Louvor e Adoração").descricao("Responsável por conduzir a igreja em momentos de louvor e adoração através da música.").build());
        ministerioRepository.save(Ministerio.builder()
                .nome("Ensino e Discipulado").descricao("Estudos bíblicos, escola dominical e discipulado de novos convertidos.").build());
        ministerioRepository.save(Ministerio.builder()
                .nome("Ação Social").descricao("Promove ações sociais, visitas a hospitais, orfanatos e comunidades carentes.").build());
        ministerioRepository.save(Ministerio.builder()
                .nome("Intercessão").descricao("Grupo de oração e intercessão pelos membros, líderes e pela cidade.").build());
        log.info("Seed: ministerios criados");
    }

    private void seedCelulas() {
        if (celulaRepository.count() > 0) return;
        celulaRepository.save(Celula.builder().nome("Célula do Centro").lider("Pr. João Silva")
                .endereco("Rua Marechal Floriano, 150 - Centro").diaSemana("Terça-feira")
                .horario(LocalTime.of(19, 30)).descricao("Grupo pequeno para estudo bíblico e comunhão.").build());
        celulaRepository.save(Celula.builder().nome("Célula Jardim Alegria").lider("Pb. Marcos Oliveira")
                .endereco("Av. Brasil, 500 - Jardim Alegria").diaSemana("Quarta-feira")
                .horario(LocalTime.of(20, 0)).descricao("Célula acolhedora focada em famílias e crianças.").build());
        celulaRepository.save(Celula.builder().nome("Célula Nova Geração").lider("Pra. Ana Beatriz")
                .endereco("Rua das Flores, 88 - Vila Nova").diaSemana("Quinta-feira")
                .horario(LocalTime.of(19, 0)).descricao("Célula voltada para jovens e adolescentes.").build());
        celulaRepository.save(Celula.builder().nome("Célula da Paz").lider("Diác. Paulo Santos")
                .endereco("Estrada do Ouro, 1200 - Parque Paz").diaSemana("Sábado")
                .horario(LocalTime.of(18, 0)).descricao("Grupo de vizinhos para oração e café comunitário.").build());
        log.info("Seed: celulas criadas");
    }

    private void seedEventos() {
        if (eventoRepository.count() > 0) return;
        int ano = LocalDate.now().getYear();
        int mes = LocalDate.now().getMonthValue();
        eventoRepository.save(Evento.builder().titulo("Culto de Domingo")
                .descricao("Culto de celebração com louvor, oração e palavra.")
                .data(LocalDate.of(ano, mes, 2)).hora(LocalTime.of(9, 0)).local("ICERT - Sede").build());
        eventoRepository.save(Evento.builder().titulo("Escola Dominical")
                .descricao("Aulas bíblicas para todas as idades.")
                .data(LocalDate.of(ano, mes, 2)).hora(LocalTime.of(10, 30)).local("ICERT - Salas").build());
        eventoRepository.save(Evento.builder().titulo("Culto de Quarta-feira")
                .descricao("Culto de oração e estudo bíblico.")
                .data(LocalDate.of(ano, mes, 5)).hora(LocalTime.of(19, 30)).local("ICERT - Sede").build());
        eventoRepository.save(Evento.builder().titulo("Culto de Domingo")
                .data(LocalDate.of(ano, mes, 9)).hora(LocalTime.of(9, 0)).local("ICERT - Sede").build());
        eventoRepository.save(Evento.builder().titulo("Culto de Jovens")
                .descricao("Encontro jovem com música, dinâmicas e palavra.")
                .data(LocalDate.of(ano, mes, 8)).hora(LocalTime.of(19, 0)).local("ICERT - Auditório").build());
        eventoRepository.save(Evento.builder().titulo("Batismo")
                .descricao("Cerimônia de batismo nas águas.")
                .data(LocalDate.of(ano, mes, 16)).hora(LocalTime.of(9, 0)).local("ICERT - Piscina").build());
        log.info("Seed: eventos criados");
    }

    private void seedProfissionais() {
        if (profissionalRepository.count() > 0) return;
        profissionalRepository.save(Profissional.builder().nome("Dr. Carlos Mendes")
                .especialidade("Clínico Geral").telefone("(21) 98765-4321")
                .email("carlos.mendes@email.com").descricao("Atendimento clínico geral para toda a família.").build());
        profissionalRepository.save(Profissional.builder().nome("Dra. Patricia Oliveira")
                .especialidade("Pediatra").telefone("(21) 97654-3210")
                .email("patricia.oliveira@email.com").descricao("Pediatra com 15 anos de experiência.").build());
        profissionalRepository.save(Profissional.builder().nome("Dr. Roberto Lima")
                .especialidade("Dentista").telefone("(21) 96543-2109")
                .email("roberto.lima@email.com").descricao("Especialista em odontologia estética.").build());
        profissionalRepository.save(Profissional.builder().nome("Ana Cristina Santos")
                .especialidade("Advogada").telefone("(21) 95432-1098")
                .email("ana.santos@email.com").descricao("Advogada especialista em direito de família.").build());
        profissionalRepository.save(Profissional.builder().nome("João Gabriel Souza")
                .especialidade("Encanador").telefone("(21) 94321-0987")
                .descricao("Encanador profissional com mais de 20 anos de experiência.").build());
        profissionalRepository.save(Profissional.builder().nome("Maria Aparecida Costa")
                .especialidade("Cabeleireira").telefone("(21) 93210-9876")
                .email("maria.costa@email.com").descricao("Salão especializado em cortes femininos e masculinos.").build());
        log.info("Seed: profissionais criados");
    }
}
