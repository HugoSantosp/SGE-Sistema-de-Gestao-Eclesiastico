package com.sg.shared.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * Service completo para gerenciamento de uploads de arquivos.
 * 
 * Funcionalidades:
 * - Validação de tipo e tamanho
 * - Compressão de imagens
 * - Thumbnails automáticos
 * - Organização por data
 * - Limpeza de arquivos órfãos
 */
@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.max-size:5242880}") // 5MB padrão
    private long maxSize;

    @Value("${app.upload.allowed-types:image/jpeg,image/png,image/gif,image/webp,application/pdf}")
    private String allowedTypes;

    @Value("${app.upload.thumbnail.size:150}")
    private int thumbnailSize;

    @Value("${app.upload.image.quality:0.85}")
    private float imageQuality;

    // Tipos de arquivo permitidos
    private static final Map<String, String> FILE_TYPES = Map.of(
        "image/jpeg", "jpg",
        "image/png", "png",
        "image/gif", "gif",
        "image/webp", "webp",
        "application/pdf", "pdf"
    );

    /**
     * Resultado do upload
     */
    public static class UploadResult {
        private String filename;
        private String url;
        private String thumbnailUrl;
        private long size;
        private String contentType;
        private String originalFilename;

        // Getters e Setters
        public String getFilename() { return filename; }
        public void setFilename(String filename) { this.filename = filename; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getThumbnailUrl() { return thumbnailUrl; }
        public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
        public long getSize() { return size; }
        public void setSize(long size) { this.size = size; }
        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }
        public String getOriginalFilename() { return originalFilename; }
        public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
    }

    /**
     * Inicializa o diretório de uploads
     */
    @jakarta.annotation.PostConstruct
    public void init() {
        try {
            Path basePath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(basePath);
            Files.createDirectories(basePath.resolve("thumbnails"));
            logger.info("=== UPLOAD CONFIG ===");
            logger.info("Diretório de uploads (relativo): {}", uploadDir);
            logger.info("Diretório de uploads (absoluto): {}", basePath);
            logger.info("Diretório existe: {}", Files.exists(basePath));
            logger.info("=====================");
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível criar o diretório de uploads: " + uploadDir, e);
        }
    }

    /**
     * Faz upload de um arquivo com validação completa
     */
    public UploadResult store(MultipartFile file) throws IOException {
        // Validações
        validateFile(file);

        // Gera nome único com organização por data
        String filename = generateFilename(file);
        
        // Cria subdiretório por data
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Path dateDir = Paths.get(uploadDir, datePath);
        Files.createDirectories(dateDir);

        // Salva o arquivo original
        Path targetPath = dateDir.resolve(filename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // Comprime se for imagem
        if (isImage(file.getContentType())) {
            compressImage(targetPath);
        }

        // Gera thumbnail se for imagem
        String thumbnailUrl = null;
        if (isImage(file.getContentType())) {
            thumbnailUrl = generateThumbnail(targetPath, filename, datePath);
        }

        // Monta resultado
        UploadResult result = new UploadResult();
        result.setFilename(filename);
        result.setUrl("/uploads/" + datePath + "/" + filename);
        result.setThumbnailUrl(thumbnailUrl);
        result.setSize(file.getSize());
        result.setContentType(file.getContentType());
        result.setOriginalFilename(file.getOriginalFilename());

        logger.info("Arquivo salvo: {} ({} bytes)", filename, file.getSize());
        
        return result;
    }

    /**
     * Valida o arquivo
     */
    private void validateFile(MultipartFile file) {
        // Verifica se está vazio
        if (file.isEmpty()) {
            throw new ValidationException("Arquivo vazio");
        }

        // Verifica tamanho
        if (file.getSize() > maxSize) {
            throw new ValidationException(
                String.format("Arquivo muito grande. Tamanho máximo: %d MB", maxSize / (1024 * 1024)));
        }

        // Verifica tipo
        String contentType = file.getContentType();
        if (contentType == null || !FILE_TYPES.containsKey(contentType)) {
            throw new ValidationException(
                "Tipo de arquivo não permitido. Tipos aceitos: " + String.join(", ", FILE_TYPES.keySet()));
        }

        // Verifica extensão
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String extension = getExtension(originalFilename);
            if (!FILE_TYPES.containsValue(extension.toLowerCase())) {
                throw new ValidationException("Extensão de arquivo não permitida: " + extension);
            }
        }
    }

    /**
     * Gera nome único para o arquivo
     */
    private String generateFilename(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        } else {
            String contentType = file.getContentType();
            if (contentType != null && FILE_TYPES.containsKey(contentType)) {
                extension = "." + FILE_TYPES.get(contentType);
            }
        }

        return UUID.randomUUID().toString() + extension;
    }

    /**
     * Comprime imagem mantendo qualidade
     */
    private void compressImage(Path imagePath) {
        try {
            BufferedImage originalImage = ImageIO.read(imagePath.toFile());
            if (originalImage == null) return;

            // Calcula nova largura mantendo proporção
            int newWidth = Math.min(originalImage.getWidth(), 1920);
            double ratio = (double) newWidth / originalImage.getWidth();
            int newHeight = (int) (originalImage.getHeight() * ratio);

            // Redimensiona
            BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = resizedImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
            g2d.dispose();

            // Salva com compressão
            String formatName = getExtension(imagePath.getFileName().toString()).replace(".", "");
            if (formatName.isEmpty()) formatName = "jpg";

            ImageIO.write(resizedImage, formatName, imagePath.toFile());
            
            logger.debug("Imagem comprimida: {} ({}x{})", imagePath.getFileName(), newWidth, newHeight);
        } catch (IOException e) {
            logger.warn("Erro ao comprimir imagem: {}", e.getMessage());
        }
    }

    /**
     * Gera thumbnail da imagem
     */
    private String generateThumbnail(Path imagePath, String filename, String datePath) {
        try {
            BufferedImage originalImage = ImageIO.read(imagePath.toFile());
            if (originalImage == null) return null;

            // Calcula dimensões do thumbnail mantendo proporção
            int origWidth = originalImage.getWidth();
            int origHeight = originalImage.getHeight();
            
            double ratio = Math.min((double) thumbnailSize / origWidth, (double) thumbnailSize / origHeight);
            int thumbWidth = (int) (origWidth * ratio);
            int thumbHeight = (int) (origHeight * ratio);

            // Cria thumbnail
            BufferedImage thumbnail = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = thumbnail.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(originalImage, 0, 0, thumbWidth, thumbHeight, null);
            g2d.dispose();

            // Salva thumbnail
            Path thumbnailDir = Paths.get(uploadDir, "thumbnails", datePath);
            Files.createDirectories(thumbnailDir);
            
            Path thumbnailPath = thumbnailDir.resolve("thumb_" + filename);
            String formatName = getExtension(filename).replace(".", "");
            if (formatName.isEmpty()) formatName = "jpg";
            
            ImageIO.write(thumbnail, formatName, thumbnailPath.toFile());
            
            logger.debug("Thumbnail criado: {}", thumbnailPath.getFileName());
            
            return "/uploads/thumbnails/" + datePath + "/thumb_" + filename;
        } catch (IOException e) {
            logger.warn("Erro ao criar thumbnail: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Remove um arquivo
     */
    public boolean deleteFile(String fileUrl) {
        try {
            // Converte URL para caminho do arquivo
            String filePath = fileUrl.replace("/uploads/", "");
            Path path = Paths.get(uploadDir, filePath);
            
            if (Files.exists(path)) {
                Files.delete(path);
                
                // Tenta remover thumbnail se existir
                Path thumbnailPath = Paths.get(uploadDir, "thumbnails", filePath);
                if (Files.exists(thumbnailPath)) {
                    Files.delete(thumbnailPath);
                }
                
                logger.info("Arquivo removido: {}", filePath);
                return true;
            }
            return false;
        } catch (IOException e) {
            logger.error("Erro ao remover arquivo: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Lista todos os arquivos
     */
    public List<Path> listFiles() throws IOException {
        List<Path> files = new ArrayList<>();
        Path uploadPath = Paths.get(uploadDir);
        
        if (Files.exists(uploadPath)) {
            Files.walk(uploadPath)
                .filter(Files::isRegularFile)
                .filter(path -> !path.toString().contains("thumbnails"))
                .forEach(files::add);
        }
        
        return files;
    }

    /**
     * Limpa arquivos órfãos (sem referência no banco)
     */
    public int cleanupOrphanedFiles(Set<String> referencedFiles) {
        int deletedCount = 0;
        
        try {
            List<Path> allFiles = listFiles();
            
            for (Path file : allFiles) {
                String fileUrl = "/uploads/" + uploadDir + "/" + file.getFileName();
                
                // Verifica se o arquivo está referenciado
                boolean isReferenced = referencedFiles.stream()
                    .anyMatch(ref -> ref.contains(file.getFileName().toString()));
                
                if (!isReferenced) {
                    Files.delete(file);
                    deletedCount++;
                    logger.info("Arquivo órfão removido: {}", file.getFileName());
                }
            }
        } catch (IOException e) {
            logger.error("Erro ao limpar arquivos órfãos: {}", e.getMessage());
        }
        
        return deletedCount;
    }

    /**
     * Obtém estatísticas de uso
     */
    public Map<String, Object> getStorageStats() throws IOException {
        Map<String, Object> stats = new HashMap<>();
        
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            stats.put("totalFiles", 0);
            stats.put("totalSize", 0);
            return stats;
        }
        
        long totalSize = 0;
        int totalFiles = 0;
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(uploadPath)) {
            for (Path entry : stream) {
                if (Files.isRegularFile(entry)) {
                    totalSize += Files.size(entry);
                    totalFiles++;
                }
            }
        }
        
        stats.put("totalFiles", totalFiles);
        stats.put("totalSize", totalSize);
        stats.put("totalSizeMB", totalSize / (1024 * 1024));
        stats.put("uploadDir", uploadDir);
        
        return stats;
    }

    /**
     * Verifica se é imagem
     */
    private boolean isImage(String contentType) {
        return contentType != null && contentType.startsWith("image/");
    }

    /**
     * Obtém extensão do arquivo
     */
    private String getExtension(String filename) {
        if (filename == null) return "";
        int lastDot = filename.lastIndexOf('.');
        return lastDot >= 0 ? filename.substring(lastDot + 1) : "";
    }

    /**
     * Exceção para validação
     */
    public static class ValidationException extends RuntimeException {
        public ValidationException(String message) {
            super(message);
        }
    }
}
