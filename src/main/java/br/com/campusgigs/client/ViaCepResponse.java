package br.com.campusgigs.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Resposta do ViaCEP.
 *
 * Atencao: quando o CEP nao existe o ViaCEP responde 200 OK com {"erro": "true"}.
 * Por isso nao basta olhar o status HTTP - precisamos inspecionar o corpo.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ViaCepResponse(
        String cep,
        String logradouro,
        String complemento,
        String bairro,
        String localidade,
        String uf,
        String erro
) {

    public boolean naoEncontrado() {
        if (erro != null && !erro.isBlank() && !"false".equalsIgnoreCase(erro)) {
            return true;
        }
        return cep == null || cep.isBlank();
    }
}
