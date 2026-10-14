# 🏛️ SGE - Sistema de Gerenciamento Eclesiástico

[![Build Status](https://img.shields.io/badge/build-pending-orange)]()
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-17-red.svg)](https://angular.io)

Sistema completo para gerenciamento de igrejas e ministérios, com funcionalidades de administração, escalas de louvor, finanças e muito mais.

---

## 📋 Índice

- [Funcionalidades](#funcionalidades)
- [Arquitetura](#arquitetura)
- [Pré-requisitos](#pré-requisitos)
- [Instalação](#instalação)
- [Configuração](#configuração)
- [Uso](#uso)
- [API Documentation](#api-documentation)
- [Deploy](#deploy)
- [Contribuição](#contribuição)
- [Licença](#licença)

---

## ✨ Funcionalidades

### Backend (Spring Boot)
- 🔐 **Autenticação JWT** com senhas temporárias
- 👥 **Gestão de Usuários** com sincronização de cargos
- ⛪ **Módulos**: Membros, Igrejas, Ministérios, Células, Eventos
- 💰 **Financeiro**: Contas a pagar/receber
- 📅 **Escalas de Louvor** com confirmação de disponibilidade
- 📊 **Relatórios** em PDF e Excel
- 📧 **Notificações por Email**
- 🛡️ **Segurança**: Rate limiting, headers, CORS
- 📈 **Monitoramento**: Health checks, métricas Prometheus

### Frontend (Angular)
- 🎨 **Interface Moderna** com identidade visual ICERT
- 📱 **Design Responsivo** para desktop e mobile
- 🔍 **Busca e Filtros** em todas as listagens
- ✅ **Validações** com mensagens em português
- 📊 **Dashboards** interativos
- 🎭 **Dois Apps**: Admin + MeuMinisterio (estilo Voluts)

---

## 🏗️ Arquitetura

```

┌──────────────────────────────────────────────────────────────────┐
│              SGE — System Architecture Overview                 │
└──────────────────────────────────────────────────────────────────┘

   ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
   │  Public Website  │  │  Admin Dashboard │  │  Meu Ministério  │
   │    (Angular)     │  │    (Angular)     │  │    (Angular)     │
   └────────┬─────────┘  └────────┬─────────┘  └────────┬─────────┘
            │                     │                     │
            └─────────────────────┼─────────────────────┘
                                  │
                           HTTPS / REST API
                                  │
                     ┌────────────▼────────────┐
                     │     Shared Backend     │
                     │   Java + Spring Boot   │
                     │   Spring Security      │
                     └──────┬──────────┬──────┘
                            │          │
                 ┌──────────▼───┐  ┌───▼────────────┐
                 │  PostgreSQL  │  │     Redis      │
                 │   Database   │  │     Cache      │
                 └──────────────┘  └────────────────┘
                            │
                     ┌──────▼──────┐
                     │    Sentry   │
                     │ Error       │
                     │ Monitoring  │
                     └─────────────┘

```

---

## 📦 Pré-requisitos

### Backend
- Java 17+
- Maven 3.8+
- PostgreSQL 15+
- Docker (opcional, para testes)

### Frontend
- Node.js 18+
- npm 9+
- Angular CLI 17+

---

### Funcionalidades Principais

#### 1. Gestão de Membros
- Cadastro completo com dados pessoais
- Upload de foto com compressão automática
- Status (ativo/inativo)
- Vinculação com ministérios

#### 2. Escalas de Louvor
- Criação de escalas por data
- Designação de membros por função
- Confirmação de disponibilidade
- Geração de links públicos

#### 3. Financeiro
- Contas a pagar e receber
- Status de pagamento
- Relatórios financeiros

#### 4. Relatórios
- Exportação em PDF e Excel
- Relatórios de membros, financeiro e escalas

---

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

---

## 🙏 Agradecimentos

- Equipe de Desenvolvimento ICERT
- Todos os contribuidores do projeto

---

**Última atualização:** Agosto 2026
