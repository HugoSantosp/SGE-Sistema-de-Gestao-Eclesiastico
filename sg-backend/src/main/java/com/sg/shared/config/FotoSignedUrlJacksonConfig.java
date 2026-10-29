package com.sg.shared.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.sg.shared.services.SignedUrlService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.List;

/**
 * Assina automaticamente, na serialização JSON, todo campo "foto" (String) das respostas da API.
 *
 * O banco continua guardando só o caminho permanente; a URL assinada (temporária) é gerada
 * a cada resposta. Caminhos externos (http/https/data) não são alterados.
 * O Spring Boot registra automaticamente beans do tipo Module no ObjectMapper.
 */
@Configuration
public class FotoSignedUrlJacksonConfig {

    private static final String FOTO_PROPERTY = "foto";

    @Bean
    public SimpleModule fotoSignedUrlModule(SignedUrlService signedUrlService) {
        SimpleModule module = new SimpleModule("FotoSignedUrlModule");
        module.setSerializerModifier(new BeanSerializerModifier() {
            @Override
            public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                                                             BeanDescription beanDesc,
                                                             List<BeanPropertyWriter> beanProperties) {
                for (BeanPropertyWriter writer : beanProperties) {
                    if (FOTO_PROPERTY.equals(writer.getName()) && String.class.equals(writer.getType().getRawClass())) {
                        writer.assignSerializer(new FotoSerializer(signedUrlService));
                    }
                }
                return beanProperties;
            }
        });
        return module;
    }

    static class FotoSerializer extends JsonSerializer<Object> {
        private final SignedUrlService signedUrlService;

        FotoSerializer(SignedUrlService signedUrlService) {
            this.signedUrlService = signedUrlService;
        }

        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(signedUrlService.signStoredPath((String) value));
        }
    }
}
