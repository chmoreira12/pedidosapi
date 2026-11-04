package com.lanchonete.pedidosapi.exception;

import java.util.List;

public record ErroResposta(
        int status,
        String mensagem,
        List<String> erros
) {
    public ErroResposta(int status, String mensagem) {
        this(status, mensagem, List.of());
    }
}
