# 📋 Changelog

Todas as mudanças notáveis neste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/),
e este projeto adere ao [Semantic Versioning](https://semver.org/lang/pt-BR/).

---

## [1.0.0] - 2026-08-26

### 🚀 Adicionado

#### Backend
- **Autenticação JWT** completa com senhas temporárias
- **Rate Limiting** com Bucket4j para proteção contra abuso
- **Logs Estruturados** com Logback (JSON, auditoria, segurança)
- **Health Checks** com Spring Boot Actuator
- **Métricas** com Micrometer + Prometheus
- **Upload de Arquivos** com validação, compressão e thumbnails
- **Envio de Emails** com templates HTML (Thymeleaf)
- **Relatórios** em PDF (iText) e Excel (Apache POI)
- **CORS Configurado** para produção
- **Headers de Segurança** (HSTS, X-Content-Type-Options, etc.)
- **Backup Script** para banco de dados

#### Frontend
- **Validações de Formulário** com mensagens em português
- **Toast Notifications** para feedback visual
- **Componente de Confirmação** antes de deletar registros
- **Paginação** em todas as listagens
- **Busca e Filtros** com debounce
- **Loading States** em todas as operações

#### Documentação
- **README Completo** com instruções de instalação
- **CHANGELOG** seguindo Keep a Changelog
- **API Documentation** com Swagger/OpenAPI
- **Guia de Deploy** para produção

### 🔧 Melhorado

- Performance do banco de dados com índices
- Validação de dados de entrada
- Tratamento de erros amigável
- UX do formulário de login

### 🐛 Corrigido

- Validação de CPF/CNPJ
- Upload de imagens grandes
- Timeout de conexão com banco

---

## [0.9.0] - 2026-08-15

### 🚀 Adicionado

- Módulo de Escalas de Louvor
- App MeuMinisterio (estilo Voluts)
- Configuração de produção

### 🔧 Melhorado

- Design da interface
- Performance de queries

---

## [0.8.0] - 2026-08-01

### 🚀 Adicionado

- Módulo Financeiro (Contas a Pagar/Receber)
- Módulo de Eventos
- Módulo de Células

---

## [0.7.0] - 2026-07-15

### 🚀 Adicionado

- Gestão de Membros
- Gestão de Ministérios
- Gestão de Cargos

---

## [0.6.0] - 2026-07-01

### 🚀 Adicionado

- Autenticação JWT
- CRUD de Usuários
- Dashboard básico

---

## [0.5.0] - 2026-06-15

### 🚀 Adicionado

- Estrutura inicial do projeto
- Configuração do banco de dados
- Template de login

---

## [0.1.0] - 2026-06-01

### 🚀 Adicionado

- Início do desenvolvimento
- Configuração do repositório

---

## 📌 Notas Versão

### Formato da Versão

Usamos Semantic Versioning: `MAJOR.MINOR.PATCH`

- **MAJOR**: Mudanças incompatíveis com versões anteriores
- **MINOR**: Funcionalidades novas (compatível com versões anteriores)
- **PATCH**: Correções de bugs (compatível com versões anteriores)

### Tags de Release

- `v1.0.0` - Versão estável para produção
- `v1.0.0-beta` - Versão de testes
- `v1.0.0-rc.1` - Release Candidate

---

## 🔗 Links Úteis

- [Repositório](https://github.com/seu-usuario/sge-backend)
- [Issues](https://github.com/seu-usuario/sge-backend/issues)
- [Documentação da API](https://docs.icert.local)
- [Deploy](https://deploy.icert.local)

---

**Última atualização:** Agosto 2026
