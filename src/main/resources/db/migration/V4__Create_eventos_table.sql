-- V4__Create_eventos_table.sql
-- Cria a tabela de eventos (calendario da landing page)

CREATE TABLE IF NOT EXISTS eventos (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    descricao TEXT,
    data DATE NOT NULL,
    hora TIME,
    local VARCHAR(200),
    igreja_id BIGINT REFERENCES igrejas(id)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_eventos_data ON eventos(data);
CREATE INDEX IF NOT EXISTS idx_eventos_igreja ON eventos(igreja_id);
