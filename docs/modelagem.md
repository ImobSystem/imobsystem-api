# Modelagem do Banco de Dados — Imob



Banco: **PostgreSQL** · ORM: **Spring Data JPA / Hibernate**

## Diagrama ER

```mermaid
erDiagram
    IMOBILIARIA ||--o{ CORRETOR   : "possui"
    IMOBILIARIA ||--o{ CLIENTE    : "possui"
    IMOBILIARIA ||--o{ IMOVEL     : "possui"
    IMOVEL      ||--o{ NEGOCIACAO : "envolve"
    CLIENTE     ||--o{ NEGOCIACAO : "participa"
    CORRETOR    ||--o{ NEGOCIACAO : "conduz"

    IMOBILIARIA {
        bigint     id PK
        varchar    nome
        varchar    cnpj
        varchar    email
        varchar    telefone
        varchar    status_plano   "enum: ATIVO, INADIMPLENTE, CANCELADO"
        varchar    plano          "enum: BASICO, PROFISSIONAL, PREMIUM"
        date       data_vencimento
    }

    CORRETOR {
        bigint     id PK
        varchar    nome
        varchar    email
        varchar    senha
        varchar    creci
        varchar    perfil         "enum: ADMIN, CORRETOR"
        bigint     imobiliaria_id FK
    }

    CLIENTE {
        bigint     id PK
        varchar    nome
        varchar    cpf
        varchar    email
        varchar    telefone
        varchar    tipo_cliente   "enum: COMPRADOR, LOCATARIO, PROPRIETARIO"
        bigint     imobiliaria_id FK
    }

    IMOVEL {
        bigint     id PK
        varchar    endereco
        varchar    cep
        double     area_m2
        varchar    finalidade     "enum: ALUGUEL, VENDA"
        varchar    status_imovel  "enum: DISPONIVEL, NEGOCIANDO, FECHADO"
        bigint     imobiliaria_id FK
    }

    NEGOCIACAO {
        bigint     id PK
        varchar    finalidade            "enum: ALUGUEL, VENDA"
        varchar    status_negocio        "enum: OPORTUNIDADE, EM_ATENDIMENTO, VISITA_AGENDADA, PROPOSTA, GANHO, PERDIDO"
        date       data_inicio
        date       data_fim
        date       data_ultima_interacao
        double     valor
        varchar    motivo_perda
        bigint     imovel_id FK
        bigint     cliente_id FK
        bigint     corretor_id FK
    }
```

## Relacionamentos

| De | Para | Cardinalidade | Observação |
|----|------|---------------|------------|
| Imobiliaria | Corretor | 1 : N | Um corretor pertence a uma imobiliária |
| Imobiliaria | Cliente | 1 : N | Um cliente pertence a uma imobiliária |
| Imobiliaria | Imovel | 1 : N | Um imóvel pertence a uma imobiliária |
| Imovel | Negociacao | 1 : N | Um imóvel pode ter várias negociações |
| Cliente | Negociacao | 1 : N | Um cliente pode ter várias negociações |
| Corretor | Negociacao | 1 : N | Um corretor conduz várias negociações |


