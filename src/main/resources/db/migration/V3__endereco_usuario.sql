-- ============================================================
-- V3 - Endereco do usuario (CP5)
-- Colunas preenchidas a partir do CEP via integracao com o ViaCEP.
-- Evolucao de schema = nova migration, o V1 nunca e alterado.
-- ============================================================

ALTER TABLE usuario ADD COLUMN cep        VARCHAR(8);
ALTER TABLE usuario ADD COLUMN logradouro VARCHAR(160);
ALTER TABLE usuario ADD COLUMN bairro     VARCHAR(120);
ALTER TABLE usuario ADD COLUMN cidade     VARCHAR(120);
ALTER TABLE usuario ADD COLUMN uf         VARCHAR(2);
