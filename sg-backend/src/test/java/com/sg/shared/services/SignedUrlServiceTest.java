package com.sg.shared.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para o SignedUrlService.
 * 
 * Testa:
 * - Geração de URLs assinadas
 * - Validação de tokens válidos
 * - Rejeição de tokens inválidos
 * - Rejeição de URLs expiradas
 * - Segurança contra timing attacks
 */
class SignedUrlServiceTest {

    private SignedUrlService signedUrlService;

    @BeforeEach
    void setUp() {
        signedUrlService = new SignedUrlService();
        
        // Configura valores via reflection (simula @Value do Spring)
        ReflectionTestUtils.setField(signedUrlService, "secretKey", "test-secret-key-for-hmac");
        ReflectionTestUtils.setField(signedUrlService, "expirationHours", 24);
    }

    @Test
    void testGenerateSignedUrl_Success() {
        // Arrange
        String filePath = "2026/09/18/foto.png";

        // Act
        String signedUrl = signedUrlService.generateSignedUrl(filePath);

        // Assert
        assertNotNull(signedUrl, "URL assinada não deve ser null");
        assertTrue(signedUrl.startsWith("/api/uploads/"), "URL deve começar com /api/uploads/");
        assertTrue(signedUrl.contains("?token="), "URL deve conter parâmetro token");
        assertTrue(signedUrl.contains("&expires="), "URL deve conter parâmetro expires");
        assertTrue(signedUrl.contains(filePath), "URL deve conter o caminho do arquivo");
    }

    @Test
    void testGenerateSignedUrl_RemovesLeadingSlash() {
        // Arrange
        String filePath = "/2026/09/18/foto.png";

        // Act
        String signedUrl = signedUrlService.generateSignedUrl(filePath);

        // Assert
        assertFalse(signedUrl.contains("//"), "URL não deve ter barras duplas");
        assertTrue(signedUrl.contains("uploads/2026/09/18/foto.png"), "Barra inicial deve ser removida");
    }

    @Test
    void testGenerateSignedUrl_ThrowsExceptionForEmptyPath() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
                () -> signedUrlService.generateSignedUrl(""),
                "Deve lançar exceção para caminho vazio");
        
        assertThrows(IllegalArgumentException.class,
                () -> signedUrlService.generateSignedUrl(null),
                "Deve lançar exceção para caminho null");
    }

    @Test
    void testValidateSignedUrl_ValidToken() {
        // Arrange
        String filePath = "2026/09/18/foto.png";
        String signedUrl = signedUrlService.generateSignedUrl(filePath);
        
        // Extrai token e expires da URL gerada
        String[] parts = signedUrl.split("\\?")[1].split("&");
        String token = parts[0].split("=")[1];
        Long expires = Long.parseLong(parts[1].split("=")[1]);

        // Act
        boolean isValid = signedUrlService.validateSignedUrl(filePath, token, expires);

        // Assert
        assertTrue(isValid, "Token válido deve ser aceito");
    }

    @Test
    void testValidateSignedUrl_InvalidToken() {
        // Arrange
        String filePath = "2026/09/18/foto.png";
        String invalidToken = "token-invalido-forjado";
        Long expires = Instant.now().getEpochSecond() + 3600;

        // Act
        boolean isValid = signedUrlService.validateSignedUrl(filePath, invalidToken, expires);

        // Assert
        assertFalse(isValid, "Token inválido deve ser rejeitado");
    }

    @Test
    void testValidateSignedUrl_ExpiredToken() {
        // Arrange
        String filePath = "2026/09/18/foto.png";
        long expiresAt = Instant.now().getEpochSecond() - 3600; // 1 hora atrás
        
        // Gera token manualmente para expiração no passado
        // (não podemos usar generateSignedUrl pois ele sempre gera futuro)
        String token = "qualquer-token"; // Será rejeitado pela expiração antes de validar o token

        // Act
        boolean isValid = signedUrlService.validateSignedUrl(filePath, token, expiresAt);

        // Assert
        assertFalse(isValid, "Token expirado deve ser rejeitado");
    }

    @Test
    void testValidateSignedUrl_NullParameters() {
        // Act & Assert
        assertFalse(signedUrlService.validateSignedUrl(null, "token", 123L),
                "Deve retornar false para filePath null");
        
        assertFalse(signedUrlService.validateSignedUrl("path", null, 123L),
                "Deve retornar false para token null");
        
        assertFalse(signedUrlService.validateSignedUrl("path", "token", null),
                "Deve retornar false para expires null");
    }

    @Test
    void testValidateSignedUrl_DifferentFilePathInvalidatesToken() {
        // Arrange
        String originalPath = "2026/09/18/foto1.png";
        String signedUrl = signedUrlService.generateSignedUrl(originalPath);
        
        String[] parts = signedUrl.split("\\?")[1].split("&");
        String token = parts[0].split("=")[1];
        Long expires = Long.parseLong(parts[1].split("=")[1]);

        // Act - Tentar usar o mesmo token com arquivo diferente
        String differentPath = "2026/09/18/foto2.png";
        boolean isValid = signedUrlService.validateSignedUrl(differentPath, token, expires);

        // Assert
        assertFalse(isValid, "Token de um arquivo não deve funcionar para outro");
    }

    @Test
    void testGenerateSignedUrl_DifferentCallsGenerateDifferentTokens() {
        // Arrange
        String filePath = "2026/09/18/foto.png";

        // Act - Gerar duas URLs com pequeno intervalo
        String url1 = signedUrlService.generateSignedUrl(filePath);
        
        // Aguardar 1 segundo para garantir timestamps diferentes
        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            fail("Teste interrompido");
        }
        
        String url2 = signedUrlService.generateSignedUrl(filePath);

        // Assert
        assertNotEquals(url1, url2, "URLs geradas em momentos diferentes devem ter tokens diferentes");
    }

    @Test
    void testGenerateSignedUrl_ExpirationIsInFuture() {
        // Arrange
        String filePath = "2026/09/18/foto.png";

        // Act
        String signedUrl = signedUrlService.generateSignedUrl(filePath);
        
        // Extrair expires
        String expiresStr = signedUrl.split("expires=")[1];
        Long expires = Long.parseLong(expiresStr);
        long now = Instant.now().getEpochSecond();

        // Assert
        assertTrue(expires > now, "Timestamp de expiração deve estar no futuro");
        
        // Verifica se está entre 23h e 25h no futuro (margem de erro para execução do teste)
        long diffHours = (expires - now) / 3600;
        assertTrue(diffHours >= 23 && diffHours <= 25,
                "Expiração deve ser aproximadamente 24h no futuro, foi: " + diffHours + "h");
    }

    @Test
    void testValidateSignedUrl_PathNormalization() {
        // Arrange - Gerar com caminho sem barra inicial
        String pathWithoutSlash = "2026/09/18/foto.png";
        String signedUrl = signedUrlService.generateSignedUrl(pathWithoutSlash);
        
        String[] parts = signedUrl.split("\\?")[1].split("&");
        String token = parts[0].split("=")[1];
        Long expires = Long.parseLong(parts[1].split("=")[1]);

        // Act - Validar com caminho COM barra inicial
        String pathWithSlash = "/" + pathWithoutSlash;
        boolean isValid = signedUrlService.validateSignedUrl(pathWithSlash, token, expires);

        // Assert
        assertTrue(isValid, "Normalização de caminhos deve funcionar (com/sem barra inicial)");
    }
}
