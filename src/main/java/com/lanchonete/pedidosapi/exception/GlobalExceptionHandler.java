package com.lanchonete.pedidosapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PedidoNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> tratarPedidoNaoEncontrado(PedidoNaoEncontradoException exception) {

        ErroResposta erro = new ErroResposta(
                HttpStatus.NOT_FOUND.value(),
                exception.getMessage()
        );

        return ResponseEntity.status(erro.status()).body(erro);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResposta> tratarJsonInvalido(HttpMessageNotReadableException exception) {
        ErroResposta erro = new ErroResposta(
            HttpStatus.BAD_REQUEST.value(),
            "O json enviado contém dados inválidos!"
        );

        return ResponseEntity.status(erro.status()).body(erro);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> tratarErroValidacao(MethodArgumentNotValidException exception) {

        List<String> erros = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();

        ErroResposta erro = new ErroResposta(
                HttpStatus.BAD_REQUEST.value(),
                "Existem campos inválidos na requisição!",
                erros
        );

        return ResponseEntity.status(erro.status()).body(erro);
    }

    @ExceptionHandler(PedidoStatusInvalidoException.class)
    public ResponseEntity<ErroResposta> tratarStatusInvalido(PedidoStatusInvalidoException exception) {

        ErroResposta erro = new ErroResposta(
                HttpStatus.CONFLICT.value(),
                exception.getMessage()
        );
        return ResponseEntity.status(erro.status()).body(erro);
    }

    @ExceptionHandler(ProdutoInativoException.class)
    public ResponseEntity<ErroResposta> tratarProdutoInativo(ProdutoInativoException exception) {

        ErroResposta erro = new ErroResposta(
                HttpStatus.UNPROCESSABLE_CONTENT.value(),
                exception.getMessage()
        );

        return ResponseEntity.status(erro.status()).body(erro);

    }

    @ExceptionHandler(ProdutoNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> tratarProdutoNaoEncontrado(ProdutoNaoEncontradoException exception) {

        ErroResposta erro = new ErroResposta(
                HttpStatus.NOT_FOUND.value(),
                exception.getMessage()
        );

        return ResponseEntity.status(erro.status()).body(erro);
    }

}
