-- V2__Seed_default_data.sql
-- Dados iniciais para o SGE

-- Cargo padrão
INSERT INTO cargos (nome) VALUES ('Membro');

-- Configurações iniciais
INSERT INTO config (nome, valor, qtd_tarefa) VALUES ('email_super_adm', 'admin@sge.com', 20);
INSERT INTO config (nome, valor, qtd_tarefa) VALUES ('nome_igreja', 'Igreja Sede', 20);
INSERT INTO config (nome, valor, qtd_tarefa) VALUES ('endereco_igreja', 'Endereço da Igreja', 20);
INSERT INTO config (nome, valor, qtd_tarefa) VALUES ('telefone_igreja', '(21) 99999-9999', 20);
INSERT INTO config (nome, valor, qtd_tarefa) VALUES ('qtd_tarefa', '20', 20);

-- Igreja Matriz padrão
INSERT INTO igrejas (nome, telefone, endereco, obs, matriz, data_cad)
VALUES ('Igreja Sede', '(21) 99999-9999', 'Endereço da Igreja', 'Igreja Matriz', 'Sim', CURRENT_DATE);

-- Bispo padrão (Super Admin)
INSERT INTO bispos (nome, email, documento, telefone, endereco, foto, data_cad)
VALUES ('Super ADM', 'admin@sge.com', '000.000.000-00', '(21) 99999-9999', 'Endereço', 'logo.png', CURRENT_DATE);

-- Usuário padrão (senha: 123 - será alterada pelo admin)
-- A senha será gerada com BCrypt ao iniciar a aplicação via DataInitializer
INSERT INTO usuario (nome, documento, email, senha, nivel, id_pessoa, foto, igreja_id)
VALUES ('Super ADM', '000.000.000-00', 'admin@sge.com', '$2a$10$placeholder', 'PASTOR_PRESIDENTE', 1, 'logo.png', 1);
