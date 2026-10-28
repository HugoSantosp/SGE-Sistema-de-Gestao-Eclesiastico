package com.sg.shared.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Serviço para gerar e validar URLs assinadas com HMAC-SHA256.
 * 
 * URLs assinadas impedem acesso não autorizado a recursos estáticos (imagens, documentos, etc.)
 * através de um token criptográfico que expira após um período configurado.
 * 
 * Formato da URL assinada:
 *   /api/uploads/2026/09/18/foto.png?token=abc123&expires=1698432000
 * 
 * Funcionamento:
 * 1. Backend gera token HMAC baseado em: caminho + timestamp de expiração + chave secreta
 * 2. Frontend recebe URL assinada válida por X horas
 * 3. Ao acessar, backend valida: token correto + não expirado
 * 4. Se inválido → 403 Forbidden
 */
@Service
public class SignedUrlService {

    private static final Logger logger = LoggerFactory.getLogger(SignedUrlService.class);
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    @Value("${app.signed-url.secret:change-this-secret-in-production}")
    private String secretKey;

    @Value("${app.signed-url.expiration-hours:24}")
    private int expirationHours;

    /**
     * Gera uma URL assinada para um caminho de arquivo.
     * 
     * @param filePath Caminho relativo do arquivo (ex: "2026/09/18/foto.png")
     * @return URL completa com token e expiração (ex: "/api/uploads/2026/09/18/foto.png?token=abc&expires=123")
     */
    public String generateSignedUrl(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("Caminho do arquivo não pode ser vazio");
        }

        // Remove barra inicial se houver
        String normalizedPath = filePath.startsWith("/") ? filePath.substring(1) : filePath;
        
        // Remove "uploads/" do início se existir (evita duplicação em /api/uploads/uploads/...)
        if (normalizedPath.startsWith("uploads/")) {
            normalizedPath = normalizedPath.substring("uploads/".length());
        }
        
        // Calcula timestamp de expiração (agora + X horas)
        long expiresAt = Instant.now().getEpochSecond() + (expirationHours * 3600L);
        
        // Gera token HMAC
        String token = generateToken(normalizedPath, expiresAt);
        
        // Monta URL completa
        return String.format("/api/uploads/%s?token=%s&expires=%d", normalizedPath, token, expiresAt);
    }

    /**
     * Valida se uma URL assinada é válida.
     * 
     * @param filePath Caminho relativo do arquivo
     * @param token Token HMAC fornecido
     * @param expiresAt Timestamp de expiração fornecido
     * @return true se válido (token correto + não expirado), false caso contrário
     */
    public boolean validateSignedUrl(String filePath, String token, Long expiresAt) {
        try {
            // Validações básicas
            if (filePath == null || token == null || expiresAt == null) {
                logger.warn("Parâmetros inválidos para validação de URL assinada");
                return false;
            }

            // Remove barra inicial se houver
            String normalizedPath = filePath.startsWith("/") ? filePath.substring(1) : filePath;
            
            // Remove "uploads/" do início se existir (mesma normalização do generateSignedUrl)
            if (normalizedPath.startsWith("uploads/")) {
                normalizedPath = normalizedPath.substring("uploads/".length());
            }

            // 1. Verifica se não expirou
            long now = Instant.now().getEpochSecond();
            if (now > expiresAt) {
                logger.warn("URL assinada expirada. Agora: {}, Expiração: {}", now, expiresAt);
                return false;
            }

            // 2. Gera token esperado com os mesmos parâmetros
            String expectedToken = generateToken(normalizedPath, expiresAt);

            // 3. Compara tokens (comparação segura contra timing attacks)
            boolean isValid = secureEquals(expectedToken, token);
            
            if (!isValid) {
                logger.warn("Token inválido para arquivo: {}", normalizedPath);
            }

            return isValid;

        } catch (Exception e) {
            logger.error("Erro ao validar URL assinada: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Gera token HMAC-SHA256 para um caminho e timestamp.
     * 
     * @param filePath Caminho normalizado do arquivo
     * @param expiresAt Timestamp de expiração
     * @return Token HMAC em hexadecimal
     */
    private String generateToken(String filePath, long expiresAt) {
        try {
            // Monta mensagem: caminho + expiração
            String message = filePath + "|" + expiresAt;
            
            // Gera HMAC-SHA256
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                secretKey.getBytes(StandardCharsets.UTF_8), 
                HMAC_ALGORITHM
            );
            mac.init(secretKeySpec);
            
            byte[] hmacBytes = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            
            // Converte para hexadecimal (formato compacto e URL-safe)
            return HexFormat.of().formatHex(hmacBytes);

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            logger.error("Erro ao gerar token HMAC: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao gerar token de segurança", e);
        }
    }

    /**
     * Comparação segura de strings que previne timing attacks.
     * 
     * @param a String 1
     * @param b String 2
     * @return true se iguais
     */
    private boolean secureEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) {
            return false;
        }
        
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
