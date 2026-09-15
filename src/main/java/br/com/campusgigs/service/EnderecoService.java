package br.com.campusgigs.service;

import br.com.campusgigs.client.ViaCepClient;
import br.com.campusgigs.client.ViaCepResponse;
import br.com.campusgigs.domain.Endereco;
import br.com.campusgigs.exception.CepNaoEncontradoException;
import br.com.campusgigs.exception.ServicoExternoIndisponivelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Resolve o endereco a partir do CEP consultando o ViaCEP pelo cliente declarativo.
 *
 * Tres desfechos possiveis, todos explicitos:
 *  - CEP existe            -> Endereco preenchido com cidade e UF
 *  - CEP nao existe        -> CepNaoEncontradoException (400)
 *  - ViaCEP fora / lento   -> ServicoExternoIndisponivelException (503)
 *
 * Em nenhum caso a operacao segue em frente com o endereco pela metade.
 */
@Service
public class EnderecoService {

    private static final Logger log = LoggerFactory.getLogger(EnderecoService.class);

    private final ViaCepClient viaCepClient;

    public EnderecoService(ViaCepClient viaCepClient) {
        this.viaCepClient = viaCepClient;
    }

    public Endereco resolverPorCep(String cepInformado) {
        String cep = normalizar(cepInformado);

        ViaCepResponse resposta;
        try {
            resposta = viaCepClient.buscarPorCep(cep);

        } catch (RestClientResponseException e) {
            // O ViaCEP responde 400 quando o formato do CEP e invalido.
            if (e.getStatusCode().value() == 400) {
                throw new CepNaoEncontradoException(cep);
            }
            throw new ServicoExternoIndisponivelException(
                    "O servico de CEP respondeu com erro (" + e.getStatusCode().value()
                            + "). Tente novamente em instantes.", e);

        } catch (ResourceAccessException e) {
            // Timeout de conexao/leitura ou falha de rede.
            log.warn("Timeout ou falha de rede ao consultar o CEP {}", cep);
            throw new ServicoExternoIndisponivelException(
                    "O servico de CEP nao respondeu a tempo. Tente novamente em instantes.", e);

        } catch (RestClientException e) {
            throw new ServicoExternoIndisponivelException(
                    "Nao foi possivel consultar o servico de CEP no momento.", e);
        }

        if (resposta == null || resposta.naoEncontrado()) {
            throw new CepNaoEncontradoException(cep);
        }

        return new Endereco(
                cep,
                resposta.logradouro(),
                resposta.bairro(),
                resposta.localidade(),
                resposta.uf());
    }

    private String normalizar(String cep) {
        String apenasDigitos = cep == null ? "" : cep.replaceAll("\\D", "");
        if (apenasDigitos.length() != 8) {
            throw new CepNaoEncontradoException(cep);
        }
        return apenasDigitos;
    }
}
