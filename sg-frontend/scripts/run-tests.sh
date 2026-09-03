#!/bin/bash

# Script para rodar todos os testes do SGE Frontend
# Uso: ./scripts/run-tests.sh [opção]

set -e

echo "🚀 Iniciando testes do SGE Frontend..."
echo "======================================"

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Função para mostrar ajuda
show_help() {
    echo "Uso: $0 [opção]"
    echo ""
    echo "Opções:"
    echo "  unit      Rodar apenas testes unitários (Karma)"
    echo "  e2e       Rodar apenas testes E2E (Cypress headless)"
    echo "  e2e:open  Abrir Cypress Test Runner"
    echo "  all       Rodar todos os testes"
    echo "  coverage  Rodar testes com relatório de cobertura"
    echo "  help      Mostrar esta ajuda"
}

# Função para rodar testes unitários
run_unit_tests() {
    echo -e "${YELLOW}📝 Rodando testes unitários (Karma)...${NC}"
    npm run test -- --watch=false --browsers=ChromeHeadless
    echo -e "${GREEN}✅ Testes unitários concluídos!${NC}"
}

# Função para rodar testes E2E
run_e2e_tests() {
    echo -e "${YELLOW}🎭 Rodando testes E2E (Cypress)...${NC}"
    
    # Verifica se o servidor está rodando
    if ! curl -s http://localhost:4200 > /dev/null; then
        echo -e "${RED}❌ Servidor não está rodando na porta 4200${NC}"
        echo "Inicie o servidor com: ng serve"
        exit 1
    fi
    
    npm run test:e2e:headless
    echo -e "${GREEN}✅ Testes E2E concluídos!${NC}"
}

# Função para abrir Cypress
open_cypress() {
    echo -e "${YELLOW}🎭 Abrindo Cypress Test Runner...${NC}"
    npm run test:e2e
}

# Função para rodar todos os testes
run_all_tests() {
    echo -e "${YELLOW}🧪 Rodando todos os testes...${NC}"
    run_unit_tests
    run_e2e_tests
    echo -e "${GREEN}🎉 Todos os testes concluídos!${NC}"
}

# Função para rodar com cobertura
run_with_coverage() {
    echo -e "${YELLOW}📊 Rodando testes com cobertura...${NC}"
    
    # Gera relatório de cobertura do Angular
    ng test --watch=false --code-coverage --browsers=ChromeHeadless
    
    # Gera relatório do Cypress (se configurado)
    # npm run test:e2e:coverage
    
    echo -e "${GREEN}✅ Relatórios gerados em:${NC}"
    echo "  - Angular: coverage/sg-frontend/"
    echo "  - Cypress: cypress/coverage/ (se configurado)"
}

# Verifica argumentos
case "${1:-help}" in
    unit)
        run_unit_tests
        ;;
    e2e)
        run_e2e_tests
        ;;
    e2e:open)
        open_cypress
        ;;
    all)
        run_all_tests
        ;;
    coverage)
        run_with_coverage
        ;;
    help|*)
        show_help
        ;;
esac

echo ""
echo -e "${GREEN}✨ Concluído!${NC}"
