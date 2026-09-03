package com.sg.shared.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Controller dedicado para servir arquivos estáticos de uploads.
 * Usa /api/uploads/** para compatibilidade com o proxy Angular.
 */
@RestController
@RequestMapping("/api/uploads")
public class UploadResourceController {

    private static final Logger logger = LoggerFactory.getLogger(UploadResourceController.class);

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @GetMapping("/**")
    public ResponseEntity<Resource> serveFile(HttpServletRequest request) {
        try {
            String requestPath = request.getRequestURI();
            String filePath = requestPath.substring("/api/uploads/".length());

            Path basePath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path fullPath = basePath.resolve(filePath).normalize();

            logger.debug("Buscando arquivo: {}", fullPath);

            if (!Files.exists(fullPath) || !Files.isRegularFile(fullPath)) {
                logger.warn("Arquivo não encontrado: {}", fullPath);
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(fullPath.toUri());

            String contentType = Files.probeContentType(fullPath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .body(resource);

        } catch (Exception e) {
            logger.error("Erro ao servir arquivo: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
