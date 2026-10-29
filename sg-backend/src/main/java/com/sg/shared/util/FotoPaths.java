package com.sg.shared.util;

/**
 * Utilitário para normalizar referências de fotos.
 *
 * O banco deve guardar apenas o caminho permanente (ex: /uploads/2026/09/18/foto.png).
 * URLs assinadas (/api/uploads/...?token=...&expires=...) são temporárias e nunca
 * devem ser persistidas: este utilitário converte qualquer uma delas de volta no caminho.
 */
public final class FotoPaths {

    private static final String API_UPLOADS = "/api/uploads/";

    private FotoPaths() {
    }

    /**
     * Converte uma URL assinada (relativa ou absoluta) no caminho permanente "/uploads/...".
     * Valores que não apontam para /api/uploads/ são devolvidos sem alteração.
     */
    public static String toStoredPath(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        int idx = value.indexOf(API_UPLOADS);
        if (idx < 0) {
            return value;
        }
        String rest = value.substring(idx + API_UPLOADS.length());
        int q = rest.indexOf('?');
        if (q >= 0) {
            rest = rest.substring(0, q);
        }
        int h = rest.indexOf('#');
        if (h >= 0) {
            rest = rest.substring(0, h);
        }
        return "/uploads/" + rest;
    }

    /** Indica se o valor é um caminho local de upload que pode ser assinado com segurança. */
    public static boolean isSignable(String storedPath) {
        if (storedPath == null || storedPath.isBlank()) {
            return false;
        }
        String lower = storedPath.toLowerCase();
        if (lower.startsWith("http:") || lower.startsWith("https:") || lower.startsWith("data:")) {
            return false;
        }
        return !storedPath.contains("..") && !storedPath.contains("\\");
    }
}
