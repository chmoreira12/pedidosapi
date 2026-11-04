package com.lanchonete.pedidosapi.mapper;

import com.lanchonete.pedidosapi.dto.PedidoResponseDTO;
import com.lanchonete.pedidosapi.model.Pedido;

public class PedidoMapper {
    public static PedidoResponseDTO toResponseDTO(Pedido pedido) {
        return new PedidoResponseDTO(
            pedido.getId(),
            pedido.getCliente(),
            pedido.getDescricao(),
            pedido.getStatus()
        );
    }
}
