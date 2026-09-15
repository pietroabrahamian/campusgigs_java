package br.com.campusgigs.client;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

/**
 * Cliente HTTP declarativo do Spring (HttpExchange).
 *
 * A interface descreve O QUE chamar; a implementacao e gerada em tempo de
 * execucao pelo HttpServiceProxyFactory (ver HttpClientConfig).
 */
@HttpExchange(url = "/ws", accept = "application/json")
public interface ViaCepClient {

    @GetExchange("/{cep}/json/")
    ViaCepResponse buscarPorCep(@PathVariable("cep") String cep);
}
