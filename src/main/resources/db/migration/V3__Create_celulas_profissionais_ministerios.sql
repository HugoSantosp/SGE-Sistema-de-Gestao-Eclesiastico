-- V3__Create_celulas_profissionais_ministerios.sql
-- Cria as tabelas de ministerios, celulas e profissionais (Mural)

CREATE TABLE IF NOT EXISTS ministerios (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descricao TEXT,
    igreja_id BIGINT REFERENCES igrejas(id)
);

CREATE TABLE IF NOT EXISTS celulas (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    lider VARCHAR(100),
    endereco VARCHAR(200),
    dia_semana VARCHAR(20),
    horario TIME,
    descricao TEXT,
    igreja_id BIGINT REFERENCES igrejas(id)
);

CREATE TABLE IF NOT EXISTS profissionais (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    especialidade VARCHAR(100),
    telefone VARCHAR(20),
    email VARCHAR(100),
    foto VARCHAR(150),
    descricao TEXT,
    igreja_id BIGINT REFERENCES igrejas(id)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_celulas_igreja ON celulas(igreja_id);
CREATE INDEX IF NOT EXISTS idx_profissionais_igreja ON profissionais(igreja_id);
CREATE INDEX IF NOT EXISTS idx_ministerios_igreja ON ministerios(igreja_id);
