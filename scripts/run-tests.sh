#!/bin/bash

# Script para rodar todos os testes do SGE Backend
# Uso: ./scripts/run-tests.sh [opção]

set -e

echo "🚀 Iniciando testes do SGE Backend..."
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
    echo "  unit      Rodar apenas testes unitários"
    echo "  integration  Rodar testes de integração (requer Docker)"
    echo "  all       Rodar todos os testes"
    echo "  coverage  Rodar testes com relatório de cobertura (JaCoCo)"
    echo "  help      Mostrar esta ajuda"
}

# Função para rodar testes unitários
run_unit_tests() {
    echo -e "${YELLOW}📝 Rodando testes unitários...${NC}"
    mvn test -Dspring.profiles.active=test
    echo -e "${GREEN}✅ Testes unitários concluídos!${NC}"
}

# Função para rodar testes de integração
run_integration_tests() {
    echo -e "${YELLOW}🔗 Rodando testes de integração (TestContainers)...${NC}"
    
    # Verifica se o Docker está rodando
    if ! docker info > /dev/null 2>&1; then
        echo -e "${RED}❌ Docker não está rodando!${NC}"
        echo "Inicie o Docker Desktop e tente novamente."
        exit 1
    fi
    
    mvn test -Dtest="*IntegrationTest" -Dspring.profiles.active=test
    echo -e "${GREEN}✅ Testes de integração concluídos!${NC}"
}

# Função para rodar todos os testes
run_all_tests() {
    echo -e "${YELLOW}🧪 Rodando todos os testes...${NC}"
    run_unit_tests
    run_integration_tests
    echo -e "${GREEN}🎉 Todos os testes concluídos!${NC}"
}

# Função para rodar com cobertura
run_with_coverage() {
    echo -e "${YELLOW}📊 Rodando testes com cobertura (JaCoCo)...${NC}"
    
    mvn clean test jacoco:report -Dspring.profiles.active=test
    
    echo -e "${GREEN}✅ Relatório de cobertura gerado em:${NC}"
    echo "  - site/jacoco/index.html"
    echo ""
    echo "  Para visualizar, abra o arquivo HTML no navegador."
}

# Função para rodar apenas testes de integração (com mais verbose)
run_integration_verbose() {
    echo -e "${YELLOW}🔗 Rodando testes de integração (verbose)...${NC}"
    
    if ! docker info > /dev/null 2>&1; then
        echo -e "${RED}❌ Docker não está rodando!${NC}"
        exit 1
    fi
    
    mvn test -Dtest="*IntegrationTest" -Dspring.profiles.active=test -X
    echo -e "${GREEN}✅ Testes de integração concluídos!${NC}"
}

# Função para limpar containers de teste
cleanup_test_containers() {
    echo -e "${YELLOW}🧹 Limpando containers de teste...${NC}"
    docker ps -a --filter "ancestor=postgres" --format "{{.ID}}" | xargs -r docker rm -f
    echo -e "${GREEN}✅ Containers limpos!${NC}"
}

# Verifica argumentos
case "${1:-help}" in
    unit)
        run_unit_tests
        ;;
    integration)
        run_integration_tests
        ;;
    all)
        run_all_tests
        ;;
    coverage)
        run_with_coverage
        ;;
    verbose)
        run_integration_verbose
        ;;
    cleanup)
        cleanup_test_containers
        ;;
    help|*)
        show_help
        ;;
esac

echo ""
echo -e "${GREEN}✨ Concluído!${NC}"
