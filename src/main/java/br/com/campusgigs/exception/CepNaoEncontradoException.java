package br.com.campusgigs.exception;

/** CEP com formato valido, mas inexistente na base do servico externo. Resulta em 400. */
public class CepNaoEncontradoException extends RuntimeException {

    public CepNaoEncontradoException(String cep) {
        super("CEP " + cep + " nao encontrado. Informe um CEP valido.");
    }
}
