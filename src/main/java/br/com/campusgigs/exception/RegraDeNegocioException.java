package br.com.campusgigs.exception;

/** Violacao de uma regra de negocio do dominio. Resulta em 409 Conflict. */
public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
