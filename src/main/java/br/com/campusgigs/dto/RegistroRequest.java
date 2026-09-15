package br.com.campusgigs.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistroRequest(

        @NotBlank(message = "O nome e obrigatorio.")
        @Size(max = 120, message = "O nome deve ter no maximo 120 caracteres.")
        String nome,

        @NotBlank(message = "O e-mail e obrigatorio.")
        @Email(message = "Informe um e-mail valido.")
        @Size(max = 160, message = "O e-mail deve ter no maximo 160 caracteres.")
        String email,

        @NotBlank(message = "A senha e obrigatoria.")
        @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres.")
        String senha,

        @Pattern(regexp = "\\d{5}-?\\d{3}", message = "O CEP deve ter 8 digitos (00000-000 ou 00000000).")
        String cep
) {
}
