package com.lanchonete.pedidosapi.dto;

import com.lanchonete.pedidosapi.model.StatusPedido;

import java.util.UUID;

public record PedidoResponseDTO(
        UUID id,
        String cliente,
        String descricao,
        StatusPedido status
) {
}
