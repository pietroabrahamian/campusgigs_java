package br.com.campusgigs.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Endereco resolvido a partir do CEP pela integracao com o ViaCEP.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Endereco {

    @Column(name = "cep", length = 8)
    private String cep;

    @Column(name = "logradouro", length = 160)
    private String logradouro;

    @Column(name = "bairro", length = 120)
    private String bairro;

    @Column(name = "cidade", length = 120)
    private String cidade;

    @Column(name = "uf", length = 2)
    private String uf;
}
