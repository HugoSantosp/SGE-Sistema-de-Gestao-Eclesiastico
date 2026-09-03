-- V7__Seed_escala_louvor.sql
-- Seed data idempotente para o módulo Escala de Louvor
-- Só insere se a tabela de escalas estiver vazia

-- ===== ESCALA DE LOUVOR =====
INSERT INTO escalas (titulo, ministerio_id, public_token, resultado_token, aberta, created_at, updated_at)
SELECT 'Escala Agosto 2026',
       (SELECT id FROM ministerios WHERE nome = 'Louvor e Adoração'),
       'escala-teste-pub-abc123def4567890abcdef',
       'escala-teste-res-xyz789uvw3216540xyzuvw',
       TRUE,
       NOW(),
       NOW()
WHERE NOT EXISTS (SELECT 1 FROM escalas WHERE titulo = 'Escala Agosto 2026');

-- ===== DATAS DA ESCALA =====
INSERT INTO escala_datas (escala_id, nome_evento, data, horario, local)
SELECT e.id, 'Culto Dominical', '2026-08-02', '09:00:00', 'Sede - São João de Meriti'
FROM escalas e WHERE e.titulo = 'Escala Agosto 2026'
AND NOT EXISTS (SELECT 1 FROM escala_datas d WHERE d.escala_id = e.id AND d.nome_evento = 'Culto Dominical');

INSERT INTO escala_datas (escala_id, nome_evento, data, horario, local)
SELECT e.id, 'Culto de Quarta', '2026-08-05', '19:30:00', 'Sede - São João de Meriti'
FROM escalas e WHERE e.titulo = 'Escala Agosto 2026'
AND NOT EXISTS (SELECT 1 FROM escala_datas d WHERE d.escala_id = e.id AND d.nome_evento = 'Culto de Quarta');

-- ===== CONFIRMAÇÕES =====
INSERT INTO escala_confirmacoes (escala_id, membro_id, nome, email, celular, created_at)
SELECT e.id, NULL, 'Carlos Silva', 'carlos.silva@email.com', '(21) 98765-4321', NOW()
FROM escalas e WHERE e.titulo = 'Escala Agosto 2026'
AND NOT EXISTS (SELECT 1 FROM escala_confirmacoes c WHERE c.escala_id = e.id AND c.nome = 'Carlos Silva');

INSERT INTO escala_confirmacoes (escala_id, membro_id, nome, email, celular, created_at)
SELECT e.id, NULL, 'Ana Oliveira', 'ana.oliveira@email.com', '(21) 97654-3210', NOW()
FROM escalas e WHERE e.titulo = 'Escala Agosto 2026'
AND NOT EXISTS (SELECT 1 FROM escala_confirmacoes c WHERE c.escala_id = e.id AND c.nome = 'Ana Oliveira');

-- ===== PIVOT: CONFIRMAÇÃO ⇢ DATAS =====
-- Carlos confirmou ambos os dias
INSERT INTO confirmacao_data (confirmacao_id, data_id)
SELECT c.id, d.id
FROM escala_confirmacoes c
JOIN escala_datas d ON d.escala_id = c.escala_id
WHERE c.nome = 'Carlos Silva'
  AND d.nome_evento IN ('Culto Dominical', 'Culto de Quarta')
  AND NOT EXISTS (
      SELECT 1 FROM confirmacao_data cd
      WHERE cd.confirmacao_id = c.id AND cd.data_id = d.id
  );

-- Ana confirmou apenas o domingo
INSERT INTO confirmacao_data (confirmacao_id, data_id)
SELECT c.id, d.id
FROM escala_confirmacoes c
JOIN escala_datas d ON d.escala_id = c.escala_id
WHERE c.nome = 'Ana Oliveira'
  AND d.nome_evento = 'Culto Dominical'
  AND NOT EXISTS (
      SELECT 1 FROM confirmacao_data cd
      WHERE cd.confirmacao_id = c.id AND cd.data_id = d.id
  );

-- ===== DESIGNAÇÕES (escala montada) =====
-- Culto Dominical: Carlos na guitarra, Ana no vocal
INSERT INTO escala_designacoes (escala_data_id, confirmacao_id, instrumento, ordem)
SELECT d.id, c.id, 'Guitarra', 0
FROM escala_datas d
JOIN escala_confirmacoes c ON c.escala_id = d.escala_id
WHERE d.nome_evento = 'Culto Dominical' AND c.nome = 'Carlos Silva'
  AND NOT EXISTS (
      SELECT 1 FROM escala_designacoes des
      WHERE des.escala_data_id = d.id AND des.confirmacao_id = c.id
  );

INSERT INTO escala_designacoes (escala_data_id, confirmacao_id, instrumento, ordem)
SELECT d.id, c.id, 'Backing Vocal', 1
FROM escala_datas d
JOIN escala_confirmacoes c ON c.escala_id = d.escala_id
WHERE d.nome_evento = 'Culto Dominical' AND c.nome = 'Ana Oliveira'
  AND NOT EXISTS (
      SELECT 1 FROM escala_designacoes des
      WHERE des.escala_data_id = d.id AND des.confirmacao_id = c.id
  );

-- Culto de Quarta: Carlos na guitarra (Ana não confirmou)
INSERT INTO escala_designacoes (escala_data_id, confirmacao_id, instrumento, ordem)
SELECT d.id, c.id, 'Guitarra', 0
FROM escala_datas d
JOIN escala_confirmacoes c ON c.escala_id = d.escala_id
WHERE d.nome_evento = 'Culto de Quarta' AND c.nome = 'Carlos Silva'
  AND NOT EXISTS (
      SELECT 1 FROM escala_designacoes des
      WHERE des.escala_data_id = d.id AND des.confirmacao_id = c.id
  );

-- ===== MÚSICAS (repertório) =====
INSERT INTO escala_musicas (escala_data_id, nome, artista, link, ordem)
SELECT d.id, 'Grande é o Senhor', 'Adhemar de Campos', 'https://www.youtube.com/watch?v=example1', 0
FROM escala_datas d WHERE d.nome_evento = 'Culto Dominical'
  AND NOT EXISTS (
      SELECT 1 FROM escala_musicas m
      WHERE m.escala_data_id = d.id AND m.nome = 'Grande é o Senhor'
  );

INSERT INTO escala_musicas (escala_data_id, nome, artista, link, ordem)
SELECT d.id, 'Te Agradeço', 'Diante do Trono', 'https://www.youtube.com/watch?v=example2', 1
FROM escala_datas d WHERE d.nome_evento = 'Culto Dominical'
  AND NOT EXISTS (
      SELECT 1 FROM escala_musicas m
      WHERE m.escala_data_id = d.id AND m.nome = 'Te Agradeço'
  );

INSERT INTO escala_musicas (escala_data_id, nome, artista, link, ordem)
SELECT d.id, 'Lindo És', 'Gabriel Guedes', NULL, 2
FROM escala_datas d WHERE d.nome_evento = 'Culto Dominical'
  AND NOT EXISTS (
      SELECT 1 FROM escala_musicas m
      WHERE m.escala_data_id = d.id AND m.nome = 'Lindo És'
  );

INSERT INTO escala_musicas (escala_data_id, nome, artista, link, ordem)
SELECT d.id, 'Rendido Estou', 'Aline Barros', 'https://open.spotify.com/track/example3', 0
FROM escala_datas d WHERE d.nome_evento = 'Culto de Quarta'
  AND NOT EXISTS (
      SELECT 1 FROM escala_musicas m
      WHERE m.escala_data_id = d.id AND m.nome = 'Rendido Estou'
  );

INSERT INTO escala_musicas (escala_data_id, nome, artista, link, ordem)
SELECT d.id, 'Ao Único', 'Ministério Zoe', NULL, 1
FROM escala_datas d WHERE d.nome_evento = 'Culto de Quarta'
  AND NOT EXISTS (
      SELECT 1 FROM escala_musicas m
      WHERE m.escala_data_id = d.id AND m.nome = 'Ao Único'
  );
