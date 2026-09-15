package br.com.campusgigs.dto;

import br.com.campusgigs.domain.Endereco;

public record EnderecoResponse(
        String cep,
        String logradouro,
        String bairro,
        String cidade,
        String uf
) {

    public static EnderecoResponse de(Endereco endereco) {
        if (endereco == null || endereco.getCep() == null) {
            return null;
        }
        return new EnderecoResponse(
                endereco.getCep(),
                endereco.getLogradouro(),
                endereco.getBairro(),
                endereco.getCidade(),
                endereco.getUf());
    }
}
