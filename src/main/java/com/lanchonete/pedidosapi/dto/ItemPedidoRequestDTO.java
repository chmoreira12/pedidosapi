package com.lanchonete.pedidosapi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ItemPedidoRequestDTO(
        @NotNull
        UUID produto,

        @NotNull
        @Min(1)
        Integer quantidade
) {
}
