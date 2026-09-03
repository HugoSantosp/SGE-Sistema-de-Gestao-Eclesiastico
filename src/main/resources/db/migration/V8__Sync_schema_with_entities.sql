-- V8__Sync_schema_with_entities.sql
-- ⚠️ INERTE: Flyway está DESABILITADO (application.yml: flyway.enabled=false).
-- O schema é gerenciado pelo Hibernate (ddl-auto: update) em dev E prod.
-- Este arquivo só roda se o Flyway for reativado um dia.
--
-- Corrige o desalinhamento entre as entidades JPA e o schema criado pelas migrations.
-- Colunas adicionadas por features posteriores (senha temporária + upload de fotos)
-- que antes eram criadas pelo Hibernate (ddl-auto: update) e não pelas migrations.

-- 1. Senha temporária (feature "senha temporária" — coluna exigida pela entidade Usuario)
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS senha_temporaria BOOLEAN NOT NULL DEFAULT FALSE;

-- 2. Foto em ministérios (feature de fotos no mural/landing page)
ALTER TABLE ministerios ADD COLUMN IF NOT EXISTS foto VARCHAR(255);

-- 3. Foto em células (feature de fotos no mural/landing page)
ALTER TABLE celulas ADD COLUMN IF NOT EXISTS foto VARCHAR(255);
