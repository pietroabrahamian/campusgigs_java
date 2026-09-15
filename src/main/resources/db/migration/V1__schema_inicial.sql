-- ============================================================
-- V1 - Schema inicial do CampusGigs (CP1)
-- Usuario, Servico e Contratacao
-- ============================================================

CREATE TABLE usuario (
    id          BIGSERIAL    PRIMARY KEY,
    nome        VARCHAR(120) NOT NULL,
    email       VARCHAR(160) NOT NULL,
    senha       VARCHAR(200) NOT NULL,
    papel       VARCHAR(20)  NOT NULL,
    criado_em   TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_usuario_email UNIQUE (email),
    CONSTRAINT ck_usuario_papel CHECK (papel IN ('ADMIN', 'USER'))
);

CREATE TABLE servico (
    id            BIGSERIAL      PRIMARY KEY,
    prestador_id  BIGINT         NOT NULL,
    titulo        VARCHAR(120)   NOT NULL,
    descricao     VARCHAR(1000)  NOT NULL,
    categoria     VARCHAR(60)    NOT NULL,
    preco         NUMERIC(10, 2) NOT NULL,
    situacao      VARCHAR(20)    NOT NULL,
    criado_em     TIMESTAMP      NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_servico_prestador FOREIGN KEY (prestador_id) REFERENCES usuario (id),
    CONSTRAINT ck_servico_situacao  CHECK (situacao IN ('ATIVO', 'PAUSADO', 'ENCERRADO')),
    CONSTRAINT ck_servico_preco     CHECK (preco > 0)
);

CREATE INDEX idx_servico_prestador ON servico (prestador_id);
CREATE INDEX idx_servico_situacao  ON servico (situacao);
CREATE INDEX idx_servico_categoria ON servico (categoria);

CREATE TABLE contratacao (
    id              BIGSERIAL      PRIMARY KEY,
    servico_id      BIGINT         NOT NULL,
    contratante_id  BIGINT         NOT NULL,
    situacao        VARCHAR(20)    NOT NULL,
    preco_acordado  NUMERIC(10, 2) NOT NULL,
    criado_em       TIMESTAMP      NOT NULL DEFAULT NOW(),
    atualizado_em   TIMESTAMP,
    CONSTRAINT fk_contratacao_servico     FOREIGN KEY (servico_id)     REFERENCES servico (id),
    CONSTRAINT fk_contratacao_contratante FOREIGN KEY (contratante_id) REFERENCES usuario (id),
    CONSTRAINT ck_contratacao_situacao    CHECK (situacao IN ('SOLICITADA', 'ACEITA', 'CONCLUIDA', 'CANCELADA'))
);

CREATE INDEX idx_contratacao_servico     ON contratacao (servico_id);
CREATE INDEX idx_contratacao_contratante ON contratacao (contratante_id);
