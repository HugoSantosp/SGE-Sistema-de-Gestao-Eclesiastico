-- V6__Create_escala_louvor_tables.sql
-- Sistema de Escala de Louvor
-- Vinculado a membros (opcional) + confirmações públicas por token

-- ===== 1. ESCALAS (principal) =====
CREATE TABLE IF NOT EXISTS escalas (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(120) NOT NULL,
    ministerio_id BIGINT,
    public_token VARCHAR(40) NOT NULL UNIQUE,
    resultado_token VARCHAR(40) UNIQUE,
    aberta BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ===== 2. DATAS DA ESCALA =====
CREATE TABLE IF NOT EXISTS escala_datas (
    id BIGSERIAL PRIMARY KEY,
    escala_id BIGINT NOT NULL REFERENCES escalas(id) ON DELETE CASCADE,
    nome_evento VARCHAR(120) NOT NULL,
    data DATE NOT NULL,
    horario TIME NOT NULL,
    local VARCHAR(120) NOT NULL
);

-- ===== 3. CONFIRMAÇÕES (integrantes) =====
CREATE TABLE IF NOT EXISTS escala_confirmacoes (
    id BIGSERIAL PRIMARY KEY,
    escala_id BIGINT NOT NULL REFERENCES escalas(id) ON DELETE CASCADE,
    membro_id BIGINT REFERENCES membros(id) ON DELETE SET NULL,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(255),
    celular VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ===== 4. PIVOT: confirmação ⇢ datas =====
CREATE TABLE IF NOT EXISTS confirmacao_data (
    confirmacao_id BIGINT NOT NULL REFERENCES escala_confirmacoes(id) ON DELETE CASCADE,
    data_id BIGINT NOT NULL REFERENCES escala_datas(id) ON DELETE CASCADE,
    PRIMARY KEY (confirmacao_id, data_id)
);

-- ===== 5. DESIGNAÇÕES (escala montada) =====
CREATE TABLE IF NOT EXISTS escala_designacoes (
    id BIGSERIAL PRIMARY KEY,
    escala_data_id BIGINT NOT NULL REFERENCES escala_datas(id) ON DELETE CASCADE,
    confirmacao_id BIGINT NOT NULL REFERENCES escala_confirmacoes(id) ON DELETE CASCADE,
    instrumento VARCHAR(60) NOT NULL,
    ordem INTEGER NOT NULL DEFAULT 0
);

-- ===== 6. MÚSICAS (repertório do ministro) =====
CREATE TABLE IF NOT EXISTS escala_musicas (
    id BIGSERIAL PRIMARY KEY,
    escala_data_id BIGINT NOT NULL REFERENCES escala_datas(id) ON DELETE CASCADE,
    nome VARCHAR(200) NOT NULL,
    artista VARCHAR(200),
    link VARCHAR(500),
    ordem INTEGER NOT NULL DEFAULT 0
);

-- ===== ÍNDICES =====
CREATE INDEX IF NOT EXISTS idx_escalas_public_token ON escalas(public_token);
CREATE INDEX IF NOT EXISTS idx_escalas_resultado_token ON escalas(resultado_token);
CREATE INDEX IF NOT EXISTS idx_escalas_ministerio_id ON escalas(ministerio_id);
CREATE INDEX IF NOT EXISTS idx_escala_datas_escala_id ON escala_datas(escala_id);
CREATE INDEX IF NOT EXISTS idx_escala_confirmacoes_escala_id ON escala_confirmacoes(escala_id);
CREATE INDEX IF NOT EXISTS idx_escala_confirmacoes_membro_id ON escala_confirmacoes(membro_id);
CREATE INDEX IF NOT EXISTS idx_escala_designacoes_data_id ON escala_designacoes(escala_data_id);
CREATE INDEX IF NOT EXISTS idx_escala_musicas_data_id ON escala_musicas(escala_data_id);
