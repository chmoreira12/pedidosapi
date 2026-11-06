package com.lanchonete.pedidosapi.exception;

public class ProdutoNaoEncontradoException extends RuntimeException{

    public ProdutoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
