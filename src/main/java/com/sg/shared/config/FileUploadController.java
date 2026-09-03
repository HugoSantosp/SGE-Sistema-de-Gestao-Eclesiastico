package com.sg.shared.config;

import com.sg.shared.services.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Controller para upload de arquivos com:
 * - Validação de tipo e tamanho
 * - Compressão de imagens
 * - Thumbnails automáticos
 * - Organização por data
 */
@RestController
@RequestMapping("/api/upload")
@Tag(name = "Upload", description = "Upload de arquivos (fotos)")
public class FileUploadController {

    private final FileStorageService fileStorageService;

    public FileUploadController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping
    @Operation(summary = "Fazer upload de uma imagem ou documento")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        try {
            FileStorageService.UploadResult result = fileStorageService.store(file);
            return ResponseEntity.ok(result);
        } catch (FileStorageService.ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Erro ao processar arquivo: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{filename}")
    @Operation(summary = "Remover um arquivo")
    public ResponseEntity<?> delete(@PathVariable String filename) {
        boolean deleted = fileStorageService.deleteFile("/uploads/" + filename);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Arquivo removido com sucesso"));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/stats")
    @Operation(summary = "Obter estatísticas de uso do armazenamento")
    public ResponseEntity<?> getStats() {
        try {
            return ResponseEntity.ok(fileStorageService.getStorageStats());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Erro ao obter estatísticas"));
        }
    }
}
