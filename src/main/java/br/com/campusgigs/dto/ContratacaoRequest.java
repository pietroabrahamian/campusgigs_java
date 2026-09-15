package br.com.campusgigs.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ContratacaoRequest(

        @NotNull(message = "Informe o id do servico a ser contratado.")
        @Positive(message = "O id do servico deve ser positivo.")
        Long servicoId
) {
}
