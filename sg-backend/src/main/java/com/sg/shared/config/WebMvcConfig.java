package com.sg.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuração MVC mínima.
 * 
 * O mapeamento de uploads é feito pelo UploadResourceController
 * para evitar conflito com o ResourceHttpRequestHandler do Spring Boot.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    // Intencionalmente vazio - uploads servidos via UploadResourceController
}
