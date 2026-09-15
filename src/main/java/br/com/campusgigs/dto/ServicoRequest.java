package br.com.campusgigs.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ServicoRequest(

        @NotBlank(message = "O titulo e obrigatorio.")
        @Size(max = 120, message = "O titulo deve ter no maximo 120 caracteres.")
        String titulo,

        @NotBlank(message = "A descricao e obrigatoria.")
        @Size(max = 1000, message = "A descricao deve ter no maximo 1000 caracteres.")
        String descricao,

        @NotBlank(message = "A categoria e obrigatoria.")
        @Size(max = 60, message = "A categoria deve ter no maximo 60 caracteres.")
        String categoria,

        @NotNull(message = "O preco e obrigatorio.")
        @DecimalMin(value = "0.01", message = "O preco deve ser maior que zero.")
        @DecimalMax(value = "99999999.99", message = "O preco informado e alto demais.")
        @Digits(integer = 8, fraction = 2, message = "O preco deve ter no maximo 2 casas decimais.")
        BigDecimal preco
) {
}
