package com.lanchonete.pedidosapi.integration;

import com.lanchonete.pedidosapi.dto.ItemPedidoRequestDTO;
import com.lanchonete.pedidosapi.dto.PedidoRequestDTO;
import com.lanchonete.pedidosapi.model.ItemPedido;
import com.lanchonete.pedidosapi.model.Pedido;
import com.lanchonete.pedidosapi.model.Produto;
import com.lanchonete.pedidosapi.model.StatusPedido;
import com.lanchonete.pedidosapi.repository.PedidoRepository;
import com.lanchonete.pedidosapi.repository.ProdutoRepository;
import com.lanchonete.pedidosapi.service.PedidoService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Testcontainers
public class PedidoCadastroIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void deveCadastrarPedidoComTresItens() {

        Produto hamburguer = new Produto();
        hamburguer.setNome("Hamburguer");
        hamburguer.setPreco(new BigDecimal("25.00"));
        hamburguer.setAtivo(true);

        Produto batata = new Produto();
        batata.setNome("Batata frita");
        batata.setPreco(new BigDecimal("15.00"));
        batata.setAtivo(true);

        Produto refrigerante = new Produto();
        refrigerante.setNome("Refrigerante");
        refrigerante.setPreco(new BigDecimal("10.00"));
        refrigerante.setAtivo(true);

        produtoRepository.saveAll(List.of(hamburguer, batata, refrigerante));

        PedidoRequestDTO dto = new PedidoRequestDTO(
                "Carlos",
                "Pedido completo",
                List.of(new ItemPedidoRequestDTO(hamburguer.getId(), 2),
                        new ItemPedidoRequestDTO(batata.getId(), 1),
                        new ItemPedidoRequestDTO(refrigerante.getId(), 3))
        );

        Pedido pedidoSalvo = pedidoService.cadastrar(dto);

        assertNotNull(pedidoSalvo.getId());

        entityManager.clear();

        Pedido pedidoEncontrado = pedidoRepository
                .findById(pedidoSalvo.getId())
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado!"));

        assertEquals(pedidoSalvo.getId(), pedidoEncontrado.getId());
        assertEquals("Carlos", pedidoEncontrado.getCliente());
        assertEquals("Pedido completo", pedidoEncontrado.getDescricao());
        assertEquals(StatusPedido.RECEBIDO, pedidoEncontrado.getStatus());

        List<ItemPedido> itens = entityManager.createQuery(
                "SELECT i FROM ItemPedido i WHERE i.pedido.id = :pedidoId",
                ItemPedido.class
        ).setParameter("pedidoId", pedidoSalvo.getId())
                .getResultList();

        assertEquals(3, itens.size());

        assertTrue(itens.stream().anyMatch(
                item -> item.getProduto().getId().equals(hamburguer.getId())
                && item.getQuantidade() == 2
                && item.getPrecoUnitatio().compareTo(new BigDecimal("25.00")) == 0
        ));

        assertTrue(itens.stream().anyMatch(
                item -> item.getProduto().getId().equals(batata.getId())
                && item.getQuantidade() == 1
                && item.getPrecoUnitatio().compareTo(new BigDecimal("15.00")) == 0
        ));

        assertTrue(itens.stream().anyMatch(
                item -> item.getProduto().getId().equals(refrigerante.getId())
                && item.getQuantidade() == 3
                && item.getPrecoUnitatio().compareTo(new BigDecimal("10.00")) == 0
        ));




    }

}
