package com.lanchonete.pedidosapi.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PedidoRequestDTO(

        @NotBlank(message = "O nome do cliente é obrigatório!")
        @Size(max=100, message = "O nome do cliente deve ter no máximo 100 caracteres!")
        String cliente,

        @NotBlank(message = "A descrição do pedido é obrigatório!")
        @Size(max = 255, message = "A descrição do pedido deve ter no máxino 255 caracteres!")
        String descricao,

        @NotEmpty
        @Valid
        List<ItemPedidoRequestDTO> item
) {
}
