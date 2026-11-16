package com.sg.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Protege a documentação OpenAPI (Swagger UI e o JSON do /api-docs) com HTTP Basic.
 *
 * <p>Existe porque a UI do Swagger é uma página de browser: ela não envia o header
 * {@code Authorization: Bearer} no carregamento inicial, então uma regra baseada em
 * JWT deixaria a tela em branco com 401. O HTTP Basic funciona porque o browser
 * pede usuário/senha e reenvia a credencial em todas as requisições da mesma origem,
 * inclusive no fetch do spec.
 *
 * <p>A cadeia só é criada quando {@code app.docs.protected=true}. Em desenvolvimento
 * a propriedade fica ausente, então o Swagger continua aberto em localhost como antes.
 *
 * <p>O {@link UserDetailsService} é construído inline e registrado apenas nesta cadeia
 * via {@code authenticationProvider(...)}. Ele não é um bean: {@code UsuarioService}
 * já implementa {@code UserDetailsService} e um segundo bean quebraria a autenticação
 * do login da aplicação.
 */
@Configuration
public class SwaggerSecurityConfig {

    /** Caminhos da documentação. Mantidos em sincronia com o springdoc em application.yml. */
    static final String[] DOCS_PATHS = {
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/api-docs/**",
            "/v3/api-docs/**",
            "/webjars/**"
    };

    @Value("${app.docs.user:docs}")
    private String docsUser;

    @Value("${app.docs.password:}")
    private String docsPassword;

    @Bean
    @Order(1)
    @ConditionalOnProperty(prefix = "app.docs", name = "protected", havingValue = "true")
    public SecurityFilterChain swaggerSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher(DOCS_PATHS)
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().hasRole("DOCS"))
                .authenticationProvider(docsAuthenticationProvider())
                .httpBasic(Customizer.withDefaults())
                .build();
    }

    /**
     * Provider restrito à cadeia de docs. Sem senha configurada o gerenciador fica
     * vazio e toda requisição recebe 401 — falha fechada, nunca aberta.
     */
    private AuthenticationProvider docsAuthenticationProvider() {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

        UserDetailsService userDetailsService;
        if (docsPassword == null || docsPassword.isBlank()) {
            userDetailsService = new InMemoryUserDetailsManager();
        } else {
            UserDetails docsUserDetails = User.withUsername(docsUser)
                    .password(encoder.encode(docsPassword))
                    .roles("DOCS")
                    .build();
            userDetailsService = new InMemoryUserDetailsManager(docsUserDetails);
        }

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(encoder);
        return provider;
    }
}
