package br.com.campusgigs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AtualizarCepRequest(

        @NotBlank(message = "O CEP e obrigatorio.")
        @Pattern(regexp = "\\d{5}-?\\d{3}", message = "O CEP deve ter 8 digitos (00000-000 ou 00000000).")
        String cep
) {
}
