package com.lanchonete.pedidosapi.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

public class StatusPedidosTest {

    @Test
    void devePermitirTransicaoDeRecebidoParaEmPreparacao() {
        StatusPedido atual = StatusPedido.RECEBIDO;
        StatusPedido novo = StatusPedido.EM_PREPARACAO;
        boolean resultado = atual.podeTransicionarpara(novo);
        assertTrue(resultado);
    }

    @Test
    void naoDevePermitirTransicaoDeRecebidoParaEntregue(){
        StatusPedido atual = StatusPedido.RECEBIDO;
        StatusPedido novo = StatusPedido.ENTREGUE;
        boolean resultado = atual.podeTransicionarpara(novo);
        assertFalse(resultado);
    }

    @ParameterizedTest
    @CsvSource({
            "RECEBIDO, EM_PREPARACAO, true",
            "RECEBIDO, CANCELADO, true",
            "RECEBIDO, ENTREGUE, false",
            "EM_PREPARACAO, PRONTO, true",
            "EM_PREPARACAO, CANCELADO, true",
            "PRONTO, ENTREGUE, true",
            "PRONTO, RECEBIDO, false",
            "ENTREGUE, RECEBIDO, false",
            "CANCELADO, EM_PREPARACAO, false"
    })
    void deveValidarTransicoesDeStatus(
            StatusPedido atual,
            StatusPedido novo,
            boolean esperado) {

        boolean resultado = atual.podeTransicionarpara(novo);
        assertEquals(resultado, esperado);

    }
}
