package br.com.campusgigs.domain;

public enum SituacaoContratacao {
    SOLICITADA,
    ACEITA,
    CONCLUIDA,
    CANCELADA;

    public boolean emAndamento() {
        return this == SOLICITADA || this == ACEITA;
    }

    public boolean finalizada() {
        return this == CONCLUIDA || this == CANCELADA;
    }
}
