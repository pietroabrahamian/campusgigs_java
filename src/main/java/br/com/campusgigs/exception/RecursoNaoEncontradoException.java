package br.com.campusgigs.exception;

/** Recurso inexistente. Resulta em 404 Not Found. */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }

    public static RecursoNaoEncontradoException de(String recurso, Object id) {
        return new RecursoNaoEncontradoException(recurso + " nao encontrado(a) para o id " + id + ".");
    }
}
