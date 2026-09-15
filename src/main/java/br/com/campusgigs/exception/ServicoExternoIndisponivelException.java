package br.com.campusgigs.exception;

/**
 * O servico externo caiu, recusou a chamada ou estourou o timeout.
 * Resulta em 503 Service Unavailable - a operacao NAO fica silenciosamente incompleta.
 */
public class ServicoExternoIndisponivelException extends RuntimeException {

    public ServicoExternoIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
