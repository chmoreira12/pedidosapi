package com.lanchonete.pedidosapi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ItemPedidoRequestDTO(
        @NotNull(message = "O id do produto é obrigatório!")
        UUID id,

        @NotNull(message = "A quantidade é obrigatória!")
        @Min(value = 1, message = "A quantidade deve ser maior que zero!")
        Integer quantidade
) {
}
