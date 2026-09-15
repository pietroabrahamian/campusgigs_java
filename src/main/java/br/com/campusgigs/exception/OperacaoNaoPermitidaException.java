package br.com.campusgigs.exception;

/**
 * O usuario esta autenticado, mas nao tem permissao sobre aquele recurso.
 * Resulta em 403 Forbidden.
 */
public class OperacaoNaoPermitidaException extends RuntimeException {

    public OperacaoNaoPermitidaException(String mensagem) {
        super(mensagem);
    }
}
