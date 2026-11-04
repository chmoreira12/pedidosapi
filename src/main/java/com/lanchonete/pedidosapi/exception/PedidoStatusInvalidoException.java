package com.lanchonete.pedidosapi.exception;

public class PedidoStatusInvalidoException extends RuntimeException {
    public PedidoStatusInvalidoException(String message) {
        super(message);
    }
}
