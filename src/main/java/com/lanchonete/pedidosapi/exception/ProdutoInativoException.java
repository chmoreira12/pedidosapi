package com.lanchonete.pedidosapi.exception;

public class ProdutoInativoException extends RuntimeException{

    public ProdutoInativoException(String mensagem) {
        super(mensagem);
    }
}
