package com.lanchonete.pedidosapi.dto;

import com.lanchonete.pedidosapi.model.StatusPedido;
import jakarta.validation.constraints.NotNull;

public record AtualizarStatusPedidoDTO(

        @NotNull(message = "O status do pedido é obrigatório!")
        StatusPedido status
) {
}
