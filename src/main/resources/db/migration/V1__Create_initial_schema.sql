-- V1__Create_initial_schema.sql
-- Migration inicial - Cria todas as tabelas do SGE

CREATE TABLE IF NOT EXISTS bispos (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(50) NOT NULL,
    email VARCHAR(50) NOT NULL,
    documento VARCHAR(20),
    telefone VARCHAR(20),
    endereco VARCHAR(150),
    foto VARCHAR(150),
    data_cad DATE,
    data_nasc DATE
);

CREATE TABLE IF NOT EXISTS cargos (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL
);

CREATE TABLE IF NOT EXISTS config (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(50) NOT NULL,
    valor VARCHAR(255) NOT NULL,
    qtd_tarefa INTEGER NOT NULL DEFAULT 20
);

CREATE TABLE IF NOT EXISTS igrejas (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    telefone VARCHAR(20) NOT NULL,
    endereco VARCHAR(150),
    obs TEXT,
    foto VARCHAR(150),
    matriz VARCHAR(5) NOT NULL DEFAULT 'Nao',
    data_cad DATE NOT NULL DEFAULT CURRENT_DATE,
    pastor_id BIGINT REFERENCES bispos(id)
);

CREATE TABLE IF NOT EXISTS presbiteros (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(50) NOT NULL,
    email VARCHAR(50) NOT NULL,
    documento VARCHAR(20),
    telefone VARCHAR(20),
    endereco VARCHAR(150),
    foto VARCHAR(150),
    data_cad DATE,
    data_nasc DATE,
    igreja_id BIGINT REFERENCES igrejas(id),
    obs TEXT
);

CREATE TABLE IF NOT EXISTS tesoureiros (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(50) NOT NULL,
    email VARCHAR(50) NOT NULL,
    documento VARCHAR(20),
    telefone VARCHAR(20),
    endereco VARCHAR(150),
    foto VARCHAR(150),
    data_cad DATE,
    data_nasc DATE,
    igreja_id BIGINT REFERENCES igrejas(id)
);

CREATE TABLE IF NOT EXISTS secretarios (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(50) NOT NULL,
    email VARCHAR(50) NOT NULL,
    documento VARCHAR(20),
    telefone VARCHAR(20),
    endereco VARCHAR(150),
    foto VARCHAR(150),
    data_cad DATE,
    data_nasc DATE,
    igreja_id BIGINT REFERENCES igrejas(id)
);

CREATE TABLE IF NOT EXISTS membros (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(50) NOT NULL,
    documento VARCHAR(20),
    telefone VARCHAR(20),
    endereco VARCHAR(150),
    foto VARCHAR(150),
    data_cad DATE,
    data_nasc DATE,
    igreja_id BIGINT NOT NULL REFERENCES igrejas(id),
    situacao VARCHAR(11) NOT NULL DEFAULT 'ATIVO',
    funcao_id BIGINT REFERENCES cargos(id),
    data_batismo DATE,
    obs TEXT
);

CREATE TABLE IF NOT EXISTS usuario (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(50) NOT NULL,
    documento VARCHAR(20),
    email VARCHAR(100) NOT NULL,
    senha VARCHAR(255) NOT NULL,
    nivel VARCHAR(50) NOT NULL,
    id_pessoa BIGINT,
    foto VARCHAR(150),
    igreja_id BIGINT REFERENCES igrejas(id)
);

CREATE TABLE IF NOT EXISTS fornecedores (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    telefone VARCHAR(20),
    endereco VARCHAR(50),
    email VARCHAR(100),
    produto VARCHAR(100),
    igreja_id BIGINT NOT NULL REFERENCES igrejas(id)
);

CREATE TABLE IF NOT EXISTS contas_pagar (
    id BIGSERIAL PRIMARY KEY,
    descricao VARCHAR(100) NOT NULL,
    fornecedor_id BIGINT REFERENCES fornecedores(id),
    valor DECIMAL(8,2) NOT NULL,
    data_cad DATE NOT NULL DEFAULT CURRENT_DATE,
    vencimento DATE NOT NULL,
    usuario_cad_id BIGINT REFERENCES usuario(id),
    usuario_baixa_id BIGINT REFERENCES usuario(id),
    data_baixa DATE,
    frequencia VARCHAR(20) NOT NULL DEFAULT 'UNICA',
    status VARCHAR(10) NOT NULL DEFAULT 'PENDENTE',
    arquivo VARCHAR(150),
    igreja_id BIGINT NOT NULL REFERENCES igrejas(id)
);

CREATE TABLE IF NOT EXISTS contas_receber (
    id BIGSERIAL PRIMARY KEY,
    descricao VARCHAR(100) NOT NULL,
    valor DECIMAL(8,2) NOT NULL,
    data_cad DATE NOT NULL DEFAULT CURRENT_DATE,
    vencimento DATE NOT NULL,
    data_recebimento DATE,
    frequencia VARCHAR(20) NOT NULL DEFAULT 'UNICA',
    status VARCHAR(10) NOT NULL DEFAULT 'PENDENTE',
    igreja_id BIGINT NOT NULL REFERENCES igrejas(id),
    contribuinte VARCHAR(150)
);

CREATE TABLE IF NOT EXISTS tarefas (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(50) NOT NULL,
    descricao VARCHAR(100),
    hora_tarefa TIME NOT NULL,
    data_tarefa DATE NOT NULL,
    status_tarefa VARCHAR(15) NOT NULL DEFAULT 'PENDENTE',
    igreja_id BIGINT NOT NULL REFERENCES igrejas(id)
);

CREATE TABLE IF NOT EXISTS notificacoes (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    atividade VARCHAR(100) NOT NULL,
    hora TIME NOT NULL,
    data_not DATE NOT NULL,
    status_not VARCHAR(50) NOT NULL
);

-- Indexes
CREATE INDEX idx_membros_igreja ON membros(igreja_id);
CREATE INDEX idx_membros_situacao ON membros(situacao);
CREATE INDEX idx_usuario_email ON usuario(email);
CREATE INDEX idx_contas_pagar_igreja ON contas_pagar(igreja_id);
CREATE INDEX idx_contas_pagar_status ON contas_pagar(status);
CREATE INDEX idx_contas_receber_igreja ON contas_receber(igreja_id);
CREATE INDEX idx_tarefas_igreja ON tarefas(igreja_id);
CREATE INDEX idx_tarefas_data ON tarefas(data_tarefa);
CREATE INDEX idx_notificacoes_data ON notificacoes(data_not);
