package com.lanchonete.pedidosapi.repository;
import com.lanchonete.pedidosapi.model.ItemPedido;
import com.lanchonete.pedidosapi.model.Pedido;
import com.lanchonete.pedidosapi.model.Produto;
import com.lanchonete.pedidosapi.model.StatusPedido;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest //Configura um ambiente de teste focado na camada de persistência JPA
@Testcontainers //Habilita a integração do Testcontainers com o JUnit 5
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // Não substitui a conexão do banco de dados por uma conexão embarcada.
public class PedidoRepositoryTest {

    @Container //Indica que o ciclo de vida do contêiner será gerenciado pelo Testcontainers
    @ServiceConnection //Permite que o Spring Boot obtenha automaticamente a conexão com o PostgreSQL do contêiner
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17");

    @Autowired
    private PedidoRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void deveSalvarPedidoNoBanco() {

        Pedido pedido = new Pedido();
        pedido.setCliente("João");
        pedido.setDescricao("Hamburguer");
        pedido.setStatus(StatusPedido.RECEBIDO);

        repository.saveAndFlush(pedido);

        entityManager.clear();

        Optional<Pedido> result = repository.findById(pedido.getId());

        assertTrue(result.isPresent());
        assertEquals("João", result.get().getCliente());
        assertEquals("Hamburguer", result.get().getDescricao());
        assertEquals(StatusPedido.RECEBIDO, result.get().getStatus());
    }

    @Test
    void deveRetornalOptionalVazioQuandoPedidoNaoExistir() {

        UUID id = UUID.randomUUID();

        Optional<Pedido> result = repository.findById(id);

        assertTrue(result.isEmpty());
    }

    @Test
    void deveBuscarPedidosPorCliente() {
        Pedido pedido1 = new Pedido();
        pedido1.setCliente("João");
        pedido1.setDescricao("Hamburguer");
        pedido1.setStatus(StatusPedido.RECEBIDO);

        Pedido pedido2 = new Pedido();
        pedido2.setCliente("Maria");
        pedido2.setDescricao("X-salada");
        pedido2.setStatus(StatusPedido.RECEBIDO);

        Pedido pedido3 = new Pedido();
        pedido3.setCliente("João");
        pedido3.setDescricao("Refri");
        pedido3.setStatus(StatusPedido.RECEBIDO);

        repository.saveAllAndFlush(List.of(pedido1, pedido2, pedido3));

        entityManager.clear();

        List<Pedido> result = repository.findByCliente("João");

        assertEquals(2, result.size());

        assertTrue(result.stream().allMatch(pedido -> pedido.getCliente().equals("João")));
    }

    @Test
    void deveBuscarPedidosPorStatus() {

        Pedido pedido1 = new Pedido();
        pedido1.setCliente("João");
        pedido1.setDescricao("Hamburguer");
        pedido1.setStatus(StatusPedido.RECEBIDO);

        Pedido pedido2 = new Pedido();
        pedido2.setCliente("Maria");
        pedido2.setDescricao("X-salada");
        pedido2.setStatus(StatusPedido.EM_PREPARACAO);

        Pedido pedido3 = new Pedido();
        pedido3.setCliente("Roberto");
        pedido3.setDescricao("Pipoca");
        pedido3.setStatus(StatusPedido.RECEBIDO);

        repository.saveAllAndFlush(List.of(pedido1, pedido2, pedido3));

        entityManager.clear();

        List<Pedido> result = repository.buscarPorStatus(StatusPedido.RECEBIDO);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(pedido -> pedido.getStatus().equals(StatusPedido.RECEBIDO)));
    }

    @Test
    void buscaPorClienteEStatus() {

        Pedido pedido1 = new Pedido();
        pedido1.setCliente("João");
        pedido1.setDescricao("X-burguer");
        pedido1.setStatus(StatusPedido.RECEBIDO);

        Pedido pedido2 = new Pedido();
        pedido2.setCliente("João");
        pedido2.setDescricao("Batata frita");
        pedido2.setStatus(StatusPedido.ENTREGUE);

        Pedido pedido3 = new Pedido();
        pedido3.setCliente("Maria");
        pedido3.setDescricao("X-salada");
        pedido3.setStatus(StatusPedido.RECEBIDO);

        Pedido pedido4 = new Pedido();
        pedido4.setCliente("João");
        pedido4.setDescricao("Refri");
        pedido4.setStatus(StatusPedido.RECEBIDO);

        repository.saveAllAndFlush(List.of(pedido1, pedido2, pedido3, pedido4));

        entityManager.clear();

        List<Pedido> result = repository.buscarPorClienteEStatus("João", StatusPedido.RECEBIDO);

        assertEquals(2, result.size());

        assertTrue(result
                .stream()
                .allMatch(pedido -> pedido.getCliente().equals("João")
                        && pedido.getStatus().equals(StatusPedido.RECEBIDO)));
    }

    @Test
    void buscarPorClienteEDoisStatus() {
        Pedido pedido1 = new Pedido();
        pedido1.setCliente("João");
        pedido1.setDescricao("Hamburguer");
        pedido1.setStatus(StatusPedido.RECEBIDO);

        Pedido pedido2 = new Pedido();
        pedido2.setCliente("João");
        pedido2.setDescricao("Refri");
        pedido2.setStatus(StatusPedido.EM_PREPARACAO);

        Pedido pedido3 = new Pedido();
        pedido3.setCliente("João");
        pedido3.setDescricao("Batata frita");
        pedido3.setStatus(StatusPedido.ENTREGUE);

        Pedido pedido4 = new Pedido();
        pedido4.setCliente("Maria");
        pedido4.setDescricao("Suco");
        pedido4.setStatus(StatusPedido.EM_PREPARACAO);

        repository.saveAllAndFlush(List.of(pedido1, pedido2, pedido3, pedido4));

        entityManager.clear();

        List<Pedido> result = repository.buscarPorClienteEDoisStatus(
                "João",
                StatusPedido.RECEBIDO,
                StatusPedido.EM_PREPARACAO);

        assertEquals(2, result.size());

        assertTrue(result
                .stream()
                .allMatch(pedido -> pedido.getCliente()
                        .equals("João") && (pedido.getStatus()
                        .equals(StatusPedido.RECEBIDO) || pedido.getStatus()
                        .equals(StatusPedido.EM_PREPARACAO))));

    }

    @Test
    void buscarPorClienteEStatus() {
        Pedido pedido1 = new Pedido();
        pedido1.setCliente("João");
        pedido1.setDescricao("Hamburguer");
        pedido1.setStatus(StatusPedido.RECEBIDO);

        Pedido pedido2 = new Pedido();
        pedido2.setCliente("João");
        pedido2.setDescricao("Hamburguer");
        pedido2.setStatus(StatusPedido.EM_PREPARACAO);

        Pedido pedido3 = new Pedido();
        pedido3.setCliente("João");
        pedido3.setDescricao("Hamburguer");
        pedido3.setStatus(StatusPedido.ENTREGUE);

        Pedido pedido4 = new Pedido();
        pedido4.setCliente("Maria");
        pedido4.setDescricao("Hamburguer");
        pedido4.setStatus(StatusPedido.RECEBIDO);

        repository.saveAllAndFlush(List.of(pedido1, pedido2, pedido3, pedido4));

        entityManager.clear();

        List<Pedido> result = repository.buscarPorClienteEStatus(
                "João",
                List.of(StatusPedido.RECEBIDO, StatusPedido.EM_PREPARACAO));

        assertEquals(2, result.size());

        assertTrue(result
                .stream()
                .allMatch(
                        pedido -> pedido.getCliente()
                                .equals("João") &&
                                List.of(
                                        StatusPedido.RECEBIDO,
                                        StatusPedido.EM_PREPARACAO)
                                        .contains(pedido.getStatus())));

    }

    @Test
    void buscarPedidosOrdenadoPorCliente() {
        Pedido pedido1 = new Pedido();
        pedido1.setCliente("Pedro");
        pedido1.setDescricao("Hamburguer");
        pedido1.setStatus(StatusPedido.RECEBIDO);

        Pedido pedido2 = new Pedido();
        pedido2.setCliente("Ana");
        pedido2.setDescricao("Hamburguer");
        pedido2.setStatus(StatusPedido.RECEBIDO);

        Pedido pedido3 = new Pedido();
        pedido3.setCliente("João");
        pedido3.setDescricao("Hamburguer");
        pedido3.setStatus(StatusPedido.RECEBIDO);

        repository.saveAllAndFlush(List.of(pedido1, pedido2, pedido3));

        entityManager.clear();

        List<Pedido> result = repository.buscarTodosOrdenadoPorCliente();

        assertEquals(3, result.size());

        assertEquals("Ana", result.get(0).getCliente());
        assertEquals("João", result.get(1).getCliente());
        assertEquals("Pedro", result.get(2).getCliente());
    }

    @Test
    void buscarPedidosOrdenadoPorClienteDecrescente() {
        Pedido pedido1 = new Pedido();
        pedido1.setCliente("Pedro");
        pedido1.setDescricao("Hamburguer");
        pedido1.setStatus(StatusPedido.RECEBIDO);

        Pedido pedido2 = new Pedido();
        pedido2.setCliente("Ana");
        pedido2.setDescricao("Hamburguer");
        pedido2.setStatus(StatusPedido.RECEBIDO);

        Pedido pedido3 = new Pedido();
        pedido3.setCliente("João");
        pedido3.setDescricao("Hamburguer");
        pedido3.setStatus(StatusPedido.RECEBIDO);

        repository.saveAllAndFlush(List.of(pedido1, pedido2, pedido3));

        entityManager.clear();

        List<Pedido> result = repository.findAll(Sort.by(Sort.Direction.DESC, "cliente"));

        assertEquals(3, result.size());

        assertEquals("Pedro", result.get(0).getCliente());
        assertEquals("João", result.get(1).getCliente());
        assertEquals("Ana", result.get(2).getCliente());
    }

    @Test
    void deveOrdenarPedidosPorStatusECliente() {

        Pedido pedido1 = new Pedido();
        pedido1.setCliente("Pedro");
        pedido1.setDescricao("Hamburguer");
        pedido1.setStatus(StatusPedido.RECEBIDO);

        Pedido pedido2 = new Pedido();
        pedido2.setCliente("João");
        pedido2.setDescricao("Hamburguer");
        pedido2.setStatus(StatusPedido.EM_PREPARACAO);

        Pedido pedido3 = new Pedido();
        pedido3.setCliente("Ana");
        pedido3.setDescricao("Hamburguer");
        pedido3.setStatus(StatusPedido.RECEBIDO);

        Pedido pedido4 = new Pedido();
        pedido4.setCliente("Maria");
        pedido4.setDescricao("Hamburguer");
        pedido4.setStatus(StatusPedido.EM_PREPARACAO);

        repository.saveAllAndFlush(List.of(pedido1, pedido2, pedido3, pedido4));

        entityManager.clear();

        Sort ordenacao = Sort.by(Sort.Direction.ASC, "status")
                .and(Sort.by(Sort.Direction.ASC, "cliente"));

        List<Pedido> result = repository.findAll(ordenacao);

        assertEquals(4, result.size());

        assertEquals("João", result.get(0).getCliente());
        assertEquals("Maria", result.get(1).getCliente());
        assertEquals("Ana", result.get(2).getCliente());
        assertEquals("Pedro", result.get(3).getCliente());
    }

    @Test
    void deveBuscarPedidosPaginados() {
        for (int i = 1; i <= 7; i++) {
            Pedido pedido = new Pedido();
            pedido.setCliente("Cliente " + i);
            pedido.setDescricao("Pedido " + 1);
            pedido.setStatus(StatusPedido.RECEBIDO);

            repository.save(pedido);
        }

        repository.flush();
        entityManager.clear();

        Pageable pageable = PageRequest.of(0, 3);

        Page<Pedido> result = repository.findAll(pageable);

        assertEquals(3, result.getContent().size());
        assertEquals(7, result.getTotalElements());
        assertEquals(3, result.getTotalPages());
        assertEquals(0, result.getNumber());
        assertEquals(3, result.getSize());
        assertTrue(result.hasNext());
    }

    @Test
    void deveBuscarPedidosPaginadosEOrdenados() {

        List<String> clientes = List.of(
                "Pedro",
                "Ana",
                "Maria",
                "Bruno",
                "João",
                "Carlos"
        );

        for(String cliente : clientes) {
            Pedido pedido = new Pedido();
            pedido.setCliente(cliente);
            pedido.setDescricao("Hamburguer");
            pedido.setStatus(StatusPedido.RECEBIDO);

            repository.save(pedido);
        }

        repository.flush();
        entityManager.clear();

        Pageable pageable = PageRequest.of(0, 3,
                Sort.by(Sort.Direction.ASC, "cliente"));

        Page<Pedido> result = repository.findAll(pageable);

        assertEquals(3, result.getContent().size());
        assertEquals(6, result.getTotalElements());
        assertEquals(2, result.getTotalPages());

        assertEquals("Ana", result.getContent().get(0).getCliente());
        assertEquals("Bruno", result.getContent().get(1).getCliente());
        assertEquals("Carlos", result.getContent().get(2).getCliente());

        assertTrue(result.hasNext());

    }

    @Test
    void deveBuscarPedidosPorStatusPaginados() {

        List<String> clientes = List.of(
                "Pedro",
                "Ana",
                "Maria",
                "Bruno",
                "João",
                "Carlos"
        );

        List<StatusPedido> status = List.of(
                StatusPedido.RECEBIDO,
                StatusPedido.RECEBIDO,
                StatusPedido.EM_PREPARACAO,
                StatusPedido.RECEBIDO,
                StatusPedido.ENTREGUE,
                StatusPedido.RECEBIDO
        );

        for(int i = 0; i < clientes.size(); i++) {
            Pedido pedido = new Pedido();
            pedido.setCliente(clientes.get(i));
            pedido.setDescricao("Hamburguer");
            pedido.setStatus(status.get(i));

            repository.save(pedido);
        }

        repository.flush();
        entityManager.clear();

        Pageable pageable = PageRequest.of(0, 2,
                Sort.by(Sort.Direction.ASC, "cliente"));

        Page<Pedido> result = repository.buscarPorStatusPaginado(StatusPedido.RECEBIDO, pageable);

        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalPages());
        assertEquals(4, result.getTotalElements());
        assertEquals("Ana", result.getContent().get(0).getCliente());
        assertEquals("Bruno", result.getContent().get(1).getCliente());
        assertTrue(result.stream().allMatch(
                pedido -> pedido.getStatus().equals(StatusPedido.RECEBIDO))
        );
        assertTrue(result.hasNext());
    }

    @Test
    void devePersistirItemPedidoComRelacionamentos() {

        //Criando um pedido
        Pedido pedido = new Pedido();
        pedido.setCliente("João");
        pedido.setDescricao("Pedido teste");
        pedido.setStatus(StatusPedido.RECEBIDO);

        entityManager.persist(pedido);

        //Cria um produto
        Produto produto = new Produto();
        produto.setNome("Hamburguer");
        produto.setPreco(new BigDecimal("20.00"));
        produto.setAtivo(true);

        entityManager.persist(produto);

        //Cria um item
        ItemPedido item = new ItemPedido();
        item.setPedido(pedido);
        item.setProduto(produto);
        item.setQuantidade(2);
        item.setPrecoUnitatio(new BigDecimal("20.00"));

        entityManager.persist(item);

        //Sincronizando com o banco
        entityManager.flush();
        entityManager.clear();

        //Busca novamente o item
        ItemPedido itemSalvo = entityManager
                .find(ItemPedido.class, item.getId());

        //verifica relacionamento
        assertNotNull(itemSalvo);
        assertEquals(pedido.getId(), itemSalvo.getPedido().getId());
        assertEquals(produto.getId(), itemSalvo.getProduto().getId());
        assertEquals(2, itemSalvo.getQuantidade());
    }

    @Test
    void devePersistirItensAutomaticamenteComCascade() {

        //Criando pedido
        Pedido pedido = new Pedido();
        pedido.setCliente("João");
        pedido.setDescricao("Pedido com cascade");
        pedido.setStatus(StatusPedido.RECEBIDO);

        //Criando produto
        Produto produto = new Produto();
        produto.setNome("Hamburguer");
        produto.setPreco(new BigDecimal("20.00"));
        produto.setAtivo(true);

        entityManager.persist(produto);

        //Criando item
        ItemPedido item = new ItemPedido();
        item.setPedido(pedido);
        item.setProduto(produto);
        item.setQuantidade(2);
        item.setPrecoUnitatio(produto.getPreco());

        pedido.getItens().add(item);

        entityManager.persist(pedido);
        entityManager.flush();
        entityManager.clear();

        ItemPedido itemSalvo = entityManager.find(ItemPedido.class, item.getId());

        assertNotNull(itemSalvo);
        assertEquals(2, itemSalvo.getQuantidade());
        assertEquals(pedido.getId(), itemSalvo.getPedido().getId());
    }

    @Test
    void deveExcluirItemQuandoRemovidoDoPedido() {

        //Criando pedido
        Pedido pedido = new Pedido();
        pedido.setCliente("João");
        pedido.setDescricao("Pedido com orphanRemoval");
        pedido.setStatus(StatusPedido.RECEBIDO);

        //Criando produto
        Produto produto = new Produto();
        produto.setNome("Hamburguer");
        produto.setPreco(new BigDecimal("20.00"));
        produto.setAtivo(true);

        entityManager.persist(produto);

        //Criando item
        ItemPedido item = new ItemPedido();
        item.setPedido(pedido);
        item.setProduto(produto);
        item.setQuantidade(2);
        item.setPrecoUnitatio(produto.getPreco());

        //add item dentro de pedido
        pedido.getItens().add(item);

        //persistindo pedido e item por cascade
        entityManager.persist(pedido);
        entityManager.flush();

        //removendo item
        pedido.getItens().remove(item);
        item.setPedido(null);

        //sincronizando alterações
        entityManager.flush();
        entityManager.clear();

        //buscando item excluido
        ItemPedido itemExcluido = entityManager.find(ItemPedido.class, item.getId());

        //verificando se foi excluido
        assertNull(itemExcluido);


    }
}
