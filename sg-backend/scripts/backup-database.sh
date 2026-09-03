#!/bin/bash

# Script de Backup Automático do Banco de Dados SGE
# Uso: ./backup-database.sh [opção]

set -e

# Configurações
BACKUP_DIR=\"./backups\"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE=\"sge_backup_${DATE}.sql\"
RETENTION_DAYS=30

# Cores para output
RED='\\033[0;31m'
GREEN='\\033[0;32m'
YELLOW='\\033[1;33m'
NC='\\033[0m'

# Função para mostrar ajuda
show_help() {
    echo \"Uso: $0 [opção]\"
    echo \"\"
    echo \"Opções:\"
    echo \"  backup     Criar backup do banco\"
    echo \"  restore    Restaurar backup mais recente\"
    echo \"  list       Listar backups disponíveis\"
    echo \"  clean      Limpar backups antigos\"
    echo \"  help       Mostrar esta ajuda\"
}

# Função para criar backup
create_backup() {
    echo -e \"${YELLOW}📦 Criando backup do banco de dados...${NC}\"
    
    # Cria diretório de backup se não existir
    mkdir -p \"$BACKUP_DIR\"
    
    # Verifica se pg_dump está disponível
    if ! command -v pg_dump &> /dev/null; then
        echo -e \"${RED}❌ pg_dump não encontrado. Instale o PostgreSQL client.${NC}\"
        exit 1
    fi
    
    # Cria o backup
    pg_dump -h localhost -U postgres -d sge -F p -f \"$BACKUP_DIR/$BACKUP_FILE\"
    
    if [ $? -eq 0 ]; then
        echo -e \"${GREEN}✅ Backup criado com sucesso: $BACKUP_DIR/$BACKUP_FILE${NC}\"
        ls -lh \"$BACKUP_DIR/$BACKUP_FILE\"
    else
        echo -e \"${RED}❌ Erro ao criar backup${NC}\"
        exit 1
    fi
}

# Função para restaurar backup
restore_backup() {
    echo -e \"${YELLOW}🔄 Restaurando backup...${NC}\"
    
    # Encontra o backup mais recente
    LATEST_BACKUP=$(ls -t \"$BACKUP_DIR\"/sge_backup_*.sql 2>/dev/null | head -n 1)
    
    if [ -z \"$LATEST_BACKUP\" ]; then
        echo -e \"${RED}❌ Nenhum backup encontrado em $BACKUP_DIR${NC}\"
        exit 1
    fi
    
    echo \"Backup selecionado: $LATEST_BACKUP\"
    
    # Confirma restauração
    read -p \"Tem certeza que deseja restaurar? Isso SUBSTITUIRÁ o banco atual. (s/N): \" -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Ss]$ ]]; then
        echo \"Restauração cancelada.\"
        exit 0
    fi
    
    # Restaura o backup
    pg_restore -h localhost -U postgres -d sge -c \"$LATEST_BACKUP\"
    
    if [ $? -eq 0 ]; then
        echo -e \"${GREEN}✅ Backup restaurado com sucesso!${NC}\"
    else
        echo -e \"${RED}❌ Erro ao restaurar backup${NC}\"
        exit 1
    fi
}

# Função para listar backups
list_backups() {
    echo -e \"${YELLOW}📋 Backups disponíveis:${NC}\"
    echo \"\"
    
    if [ ! -d \"$BACKUP_DIR\" ] || [ -z \"$(ls -A $BACKUP_DIR 2>/dev/null)\" ]; then
        echo \"Nenhum backup encontrado.\"
        return
    fi
    
    ls -lht \"$BACKUP_DIR\"/sge_backup_*.sql
    echo \"\"
    echo -e \"${GREEN}Total: $(ls \"$BACKUP_DIR\"/sge_backup_*.sql | wc -l) backup(s)${NC}\"
}

# Função para limpar backups antigos
cleanup_backups() {
    echo -e \"${YELLOW}🧹 Limpando backups antigos (>${RETENTION_DAYS} dias)...${NC}\"
    
    if [ ! -d \"$BACKUP_DIR\" ]; then
        echo \"Diretório de backup não existe.\"
        return
    fi
    
    # Encontra e remove backups antigos
    find \"$BACKUP_DIR\" -name \"sge_backup_*.sql\" -mtime +$RETENTION_DAYS -delete
    
    echo -e \"${GREEN}✅ Limpeza concluída!${NC}\"
    echo -e \"${GREEN}Backups restantes: $(ls \"$BACKUP_DIR\"/sge_backup_*.sql 2>/dev/null | wc -l)${NC}\"
}

# Verifica argumentos
case \"${1:-help}\" in
    backup)
        create_backup
        ;;
    restore)
        restore_backup
        ;;
    list)
        list_backups
        ;;
    clean)
        cleanup_backups
        ;;
    help|*)
        show_help
        ;;
esac

echo \"\"
echo -e \"${GREEN}✨ Concluído!${NC}\"
