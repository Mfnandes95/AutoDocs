# 📄 AutoDocs

> **Automação Inteligente de Termos de Responsabilidade e Gestão de Inventários.**

O **AutoDocs** é uma solução desenvolvida para automatizar a geração de documentos operacionais e o controle de inventário de equipamentos IT. Ele substitui processos manuais repetitivos por um fluxo ágil, permitindo a pré-visualização em tempo real e o preenchimento dinâmico de modelos DOCX/PDF integrados a um banco de dados relacional.

---

## 🚀 Tecnologias Utilizadas

### **Backend**
- **Java 21** – Recursos modernos de linguagem e suporte a Threads Virtuais.
- **Spring Boot 3.x** – Framework base para criação das APIs REST.
- **Spring Data JPA** – Abstração da camada de persistência.
- **POI-TL (Apache POI Template Language)** – Mecanismo de renderização de templates `.docx` baseado em tags (como Handlebars/Mustache).
- **PostgreSQL** – Banco de dados relacional para armazenamento de itens, termos e históricos.

### **Frontend & Integração**
- **JavaScript (ES6+)** – Lógica de manipulação de formulários, pré-visualização e comunicação com a API REST.
- **HTML5 & CSS3** – Interface limpa e adaptativa para preenchimento de dados operacionais.

---

## 📌 Principais Funcionalidades

- **Gerenciamento de Inventário:** Cadastro e controle de status de equipamentos (notebooks, desktops, periféricos).
- **Geração Automática de Termos:** Emissão instantânea de Termos de Responsabilidade (Empréstimo, Devolução, Transferência) preenchidos dinamicamente.
- **Motor de Templates com POI-TL:** Suporte a tags complexas em arquivos `.docx` (tabelas dinâmicas, dados do funcionário, listas de itens e assinaturas).
- **Preview e Exportação:** Visualização/download dos documentos gerados nos formatos `.docx` ou `.pdf`.
- **Histórico de Ocorrências:** Rastreabilidade dos termos emitidos por colaborador e equipamento.

---

## 📐 Arquitetura do Sistema

O projeto adota os princípios da **Arquitetura Hexagonal (Ports & Adapters)** e **Clean Architecture**, garantindo desacoplamento entre as regras de negócio, a camada Web/API e os adaptadores de templates/banco de dados.
