package com.lanchonete.pedidosapi.model;

public enum StatusPedido {
    RECEBIDO,
    EM_PREPARACAO,
    PRONTO,
    ENTREGUE,
    CANCELADO;

    public boolean podeTransicionarpara(StatusPedido novo) {

        return switch (this) {

            case RECEBIDO ->
                novo == EM_PREPARACAO ||
                novo == CANCELADO;

            case EM_PREPARACAO ->
                novo == PRONTO ||
                novo == CANCELADO;

            case PRONTO ->
                novo == ENTREGUE;

            case ENTREGUE, CANCELADO -> false;
        };
    }
}
