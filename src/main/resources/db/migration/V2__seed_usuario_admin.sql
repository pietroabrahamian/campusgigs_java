-- ============================================================
-- V2 - Usuario ADMIN inicial (CP4)
-- Sem um ADMIN em banco nao ha como demonstrar as regras de papel.
-- Senha em texto puro (apenas para o ambiente de avaliacao): admin123
-- Hash gerado com BCrypt (custo 10).
-- ============================================================

INSERT INTO usuario (nome, email, senha, papel)
VALUES (
    'Administrador CampusGigs',
    'admin@campusgigs.com',
    '$2b$10$HzEyvoLHPcSRK8KPeuuhxOUq9g9rwQ.1oVEOgdujF6IUcXjk5.atq',
    'ADMIN'
);
