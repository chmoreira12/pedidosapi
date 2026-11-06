package com.lanchonete.pedidosapi.controller;

import com.lanchonete.pedidosapi.dto.PedidoRequestDTO;
import com.lanchonete.pedidosapi.exception.ProdutoInativoException;
import com.lanchonete.pedidosapi.service.PedidoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PedidoController.class)
public class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PedidoService pedidoService;

    @Test
    void deveCarregarContextoMvc() {
        assertNotNull(mockMvc);
    }

    @Test
    void deveRetornar422QuandoProdutoEstiverInativo() throws Exception{

        UUID produtoId = UUID.randomUUID();

        String json = """
                {
                    "cliente": "Carlos",
                    "descricao": "Pedido de hamburguer",
                    "item": [
                        {
                            "id": "%s",
                            "quantidade": 2
                        }
                    ]
                }
                """.formatted(produtoId);

        //SIMULANDO COMPORTAMENTO
        when(pedidoService.cadastrar(any(PedidoRequestDTO.class)))
                .thenThrow(new ProdutoInativoException("O produto Hamburguer está inativo!"));

        mockMvc.perform(post("/pedidos") //caminho da request
                .contentType(MediaType.APPLICATION_JSON) //informa que esta mandando um json
                .content(json)) //manda o corpo da requisição
                .andExpect(status().isUnprocessableEntity()) //espera que status seja Unprocessable Entity
                .andExpect(jsonPath("$.status").value(422)) //espera que statusCode seja 422
                .andExpect(jsonPath("$.mensagem").value("O produto Hamburguer está inativo!")) //Espera que mensagem seja essa
                .andExpect(jsonPath("$.erros").isArray()); //Espera que erros seja um array
                //JsonPath permite acessar propriedades do json enviado
    }

    @Test
    void naoDeveCadastrarPedidoComQuantidadeNegativa() throws Exception {

        UUID produtoId = UUID.randomUUID();

        String json = """
                {
                    "cliente": "Carlos",
                    "descricao": "Pedido de hamburguer",
                    "item": [
                        {
                            "id": "%s",
                            "quantidade": -2
                        }
                    ]
                }
                """.formatted(produtoId);

        mockMvc.perform(post("/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("Existem campos inválidos na requisição!"))
                .andExpect(jsonPath("$.erros[0]").value("A quantidade deve ser maior que zero!"));

        verify(pedidoService, never()).cadastrar(any(PedidoRequestDTO.class));
    }

    @Test
    void naoDeveCadastrarPedidoComListaDeItensVazia() throws Exception {

        UUID produtoId = UUID.randomUUID();

        String json = """
                {
                    "cliente": "Carlos",
                    "descricao": "Pedido sem itens",
                    "item": []
                }
                """;

        mockMvc.perform(post("/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("Existem campos inválidos na requisição!"))
                .andExpect(jsonPath("$.erros[0]").value("O pedido deve conter pelo menos 1 item!"));


        verify(pedidoService, never()).cadastrar(any(PedidoRequestDTO.class));
    }

}
