-- ============================================================
-- fix_schema_sem_igreja.sql
-- Remove a estrutura antiga de multi-igrejas após a refatoração
-- para sistema de IGREJA ÚNICA (o Hibernate nunca dropa colunas).
--
-- ATENÇÃO: rode em um banco de DEV (ex.: sge-dev) e faça backup
-- antes se houver dados que você não quer perder.
-- ============================================================

-- 1) Remove as colunas igreja_id (FK) de todas as tabelas
ALTER TABLE membros        DROP COLUMN IF EXISTS igreja_id;
ALTER TABLE fornecedores   DROP COLUMN IF EXISTS igreja_id;
ALTER TABLE contas_pagar   DROP COLUMN IF EXISTS igreja_id;
ALTER TABLE contas_receber DROP COLUMN IF EXISTS igreja_id;
ALTER TABLE tarefas        DROP COLUMN IF EXISTS igreja_id;
ALTER TABLE presbiteros    DROP COLUMN IF EXISTS igreja_id;
ALTER TABLE tesoureiros    DROP COLUMN IF EXISTS igreja_id;
ALTER TABLE secretarios    DROP COLUMN IF EXISTS igreja_id;
ALTER TABLE eventos        DROP COLUMN IF EXISTS igreja_id;
ALTER TABLE ministerios    DROP COLUMN IF EXISTS igreja_id;
ALTER TABLE celulas        DROP COLUMN IF EXISTS igreja_id;
ALTER TABLE profissionais  DROP COLUMN IF EXISTS igreja_id;
ALTER TABLE usuario        DROP COLUMN IF EXISTS igreja_id;
ALTER TABLE escalas        DROP COLUMN IF EXISTS igreja_id;

-- 2) Remove a tabela de igrejas (sem referências restantes)
DROP TABLE IF EXISTS igrejas;
