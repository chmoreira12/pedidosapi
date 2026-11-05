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
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

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

    @Autowired
    private TransactionTemplate transactionTemplate;

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

    @Test
    void naoDeveCadastrarPedidoComProdutoInexistente() {

        Produto hamburguer = new Produto();
        hamburguer.setNome("Hamburguer");
        hamburguer.setPreco(new BigDecimal("25.00"));
        hamburguer.setAtivo(true);

        produtoRepository.save(hamburguer);

        UUID produtoInexistenteId = UUID.randomUUID();

        PedidoRequestDTO dto = new PedidoRequestDTO(
                "Carlos",
                "Pedido com produto inexistente",
                List.of(new ItemPedidoRequestDTO(hamburguer.getId(), 2),
                        new ItemPedidoRequestDTO(produtoInexistenteId, 1))
        );

        long quantidadeAntes = pedidoRepository.count();

        assertThrows(RuntimeException.class,
                () -> pedidoService.cadastrar(dto));

        long quantidadeDepois = pedidoRepository.count();

        assertEquals(quantidadeAntes, quantidadeDepois);
    }

    @Test
    void deveDesfazerGravacaoQuandoOcorrerExcecao() {

        long quantidadeAntes = pedidoRepository.count();

        Pedido pedido = new Pedido();
        pedido.setCliente("Carlos");
        pedido.setDescricao("Pedido para testar rollback!");
        pedido.setStatus(StatusPedido.RECEBIDO);


        assertThrows(RuntimeException.class, () -> {
            transactionTemplate.executeWithoutResult(status -> {
                pedidoRepository.saveAndFlush(pedido);
                throw new RuntimeException("Falha simulada!");
            });
        });

        long quantidadeDepois = pedidoRepository.count();

        assertEquals(quantidadeAntes, quantidadeDepois);

    }

}
