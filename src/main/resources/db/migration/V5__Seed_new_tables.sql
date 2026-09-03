-- V5__Seed_new_tables.sql
-- Seed data idempotente para as novas tabelas
-- Só insere se as tabelas estiverem vazias (evita conflito com DataInitializer)

-- ===== MINISTÉRIOS =====
INSERT INTO ministerios (nome, descricao, igreja_id)
SELECT 'Louvor e Adoração', 'Ministério responsável por conduzir a igreja em momentos de louvor e adoração através da música.', 1
WHERE NOT EXISTS (SELECT 1 FROM ministerios WHERE nome = 'Louvor e Adoração');

INSERT INTO ministerios (nome, descricao, igreja_id)
SELECT 'Ensino e Discipulado', 'Responsável pelos estudos bíblicos, escola dominical e discipulado de novos convertidos.', 1
WHERE NOT EXISTS (SELECT 1 FROM ministerios WHERE nome = 'Ensino e Discipulado');

INSERT INTO ministerios (nome, descricao, igreja_id)
SELECT 'Ação Social', 'Ministério que promove ações sociais, visitas a hospitais, orfanatos e comunidades carentes.', 1
WHERE NOT EXISTS (SELECT 1 FROM ministerios WHERE nome = 'Ação Social');

INSERT INTO ministerios (nome, descricao, igreja_id)
SELECT 'Intercessão', 'Grupo de oração e intercessão pelos membros, líderes e pela cidade.', 1
WHERE NOT EXISTS (SELECT 1 FROM ministerios WHERE nome = 'Intercessão');

-- ===== CÉLULAS =====
INSERT INTO celulas (nome, lider, endereco, dia_semana, horario, descricao, igreja_id)
SELECT 'Célula do Centro', 'Pr. João Silva', 'Rua Marechal Floriano, 150 - Centro', 'Terça-feira', '19:30:00', 'Grupo pequeno que se reúne no centro da cidade para estudo bíblico e comunhão.', 1
WHERE NOT EXISTS (SELECT 1 FROM celulas WHERE nome = 'Célula do Centro');

INSERT INTO celulas (nome, lider, endereco, dia_semana, horario, descricao, igreja_id)
SELECT 'Célula Jardim Alegria', 'Pb. Marcos Oliveira', 'Av. Brasil, 500 - Jardim Alegria', 'Quarta-feira', '20:00:00', 'Célula acolhedora focada em famílias e crianças.', 1
WHERE NOT EXISTS (SELECT 1 FROM celulas WHERE nome = 'Célula Jardim Alegria');

INSERT INTO celulas (nome, lider, endereco, dia_semana, horario, descricao, igreja_id)
SELECT 'Célula Nova Geração', 'Pra. Ana Beatriz', 'Rua das Flores, 88 - Vila Nova', 'Quinta-feira', '19:00:00', 'Célula voltada para jovens e adolescentes.', 1
WHERE NOT EXISTS (SELECT 1 FROM celulas WHERE nome = 'Célula Nova Geração');

INSERT INTO celulas (nome, lider, endereco, dia_semana, horario, descricao, igreja_id)
SELECT 'Célula da Paz', 'Diác. Paulo Santos', 'Estrada do Ouro, 1200 - Parque Paz', 'Sábado', '18:00:00', 'Grupo de vizinhos que se reúne aos sábados para oração e café comunitário.', 1
WHERE NOT EXISTS (SELECT 1 FROM celulas WHERE nome = 'Célula da Paz');

-- ===== PROFISSIONAIS (MURAL) =====
INSERT INTO profissionais (nome, especialidade, telefone, email, descricao, igreja_id)
SELECT 'Dr. Carlos Mendes', 'Clínico Geral', '(21) 98765-4321', 'carlos.mendes@email.com', 'Atendimento clínico geral para toda a família.', 1
WHERE NOT EXISTS (SELECT 1 FROM profissionais WHERE nome = 'Dr. Carlos Mendes');

INSERT INTO profissionais (nome, especialidade, telefone, email, descricao, igreja_id)
SELECT 'Dra. Patricia Oliveira', 'Pediatra', '(21) 97654-3210', 'patricia.oliveira@email.com', 'Pediatra com 15 anos de experiência.', 1
WHERE NOT EXISTS (SELECT 1 FROM profissionais WHERE nome = 'Dra. Patricia Oliveira');

INSERT INTO profissionais (nome, especialidade, telefone, email, descricao, igreja_id)
SELECT 'Dr. Roberto Lima', 'Dentista', '(21) 96543-2109', 'roberto.lima@email.com', 'Especialista em odontologia estética e clínica geral.', 1
WHERE NOT EXISTS (SELECT 1 FROM profissionais WHERE nome = 'Dr. Roberto Lima');

INSERT INTO profissionais (nome, especialidade, telefone, email, descricao, igreja_id)
SELECT 'Ana Cristina Santos', 'Advogada', '(21) 95432-1098', 'ana.santos@email.com', 'Advogada especialista em direito de família.', 1
WHERE NOT EXISTS (SELECT 1 FROM profissionais WHERE nome = 'Ana Cristina Santos');

INSERT INTO profissionais (nome, especialidade, telefone, email, descricao, igreja_id)
SELECT 'João Gabriel Souza', 'Encanador', '(21) 94321-0987', null, 'Encanador profissional com mais de 20 anos de experiência.', 1
WHERE NOT EXISTS (SELECT 1 FROM profissionais WHERE nome = 'João Gabriel Souza');

INSERT INTO profissionais (nome, especialidade, telefone, email, descricao, igreja_id)
SELECT 'Maria Aparecida Costa', 'Cabeleireira', '(21) 93210-9876', 'maria.costa@email.com', 'Salão especializado em cortes femininos e masculinos.', 1
WHERE NOT EXISTS (SELECT 1 FROM profissionais WHERE nome = 'Maria Aparecida Costa');
