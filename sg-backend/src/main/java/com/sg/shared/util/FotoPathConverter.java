package com.sg.shared.util;

import jakarta.persistence.AttributeConverter;

/**
 * Garante que apenas o caminho permanente da foto seja gravado no banco,
 * mesmo que o cliente reenvie uma URL assinada recebida da API.
 */
public class FotoPathConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return FotoPaths.toStoredPath(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return dbData;
    }
}
