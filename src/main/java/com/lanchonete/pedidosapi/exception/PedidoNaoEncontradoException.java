package com.lanchonete.pedidosapi.exception;

public class PedidoNaoEncontradoException extends RuntimeException{
    public PedidoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
