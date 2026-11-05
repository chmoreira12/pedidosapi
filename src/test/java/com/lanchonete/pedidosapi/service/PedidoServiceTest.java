package com.lanchonete.pedidosapi.service;

import com.lanchonete.pedidosapi.dto.AtualizarStatusPedidoDTO;
import com.lanchonete.pedidosapi.dto.ItemPedidoRequestDTO;
import com.lanchonete.pedidosapi.dto.PedidoRequestDTO;
import com.lanchonete.pedidosapi.dto.PedidoResponseDTO;
import com.lanchonete.pedidosapi.exception.PedidoNaoEncontradoException;
import com.lanchonete.pedidosapi.exception.PedidoStatusInvalidoException;
import com.lanchonete.pedidosapi.model.ItemPedido;
import com.lanchonete.pedidosapi.model.Pedido;
import com.lanchonete.pedidosapi.model.Produto;
import com.lanchonete.pedidosapi.model.StatusPedido;
import com.lanchonete.pedidosapi.repository.PedidoRepository;
import com.lanchonete.pedidosapi.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.engine.TestExecutionResult;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.text.html.Option;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) //integra o JUnit ao Mockito
public class PedidoServiceTest {

    @Mock //cria o repository simulado
    private PedidoRepository repository;

    @InjectMocks //cria uma instancia de service e injeta o repository
    private PedidoService service;

    @Mock
    private ProdutoRepository produtoRepository;

    private UUID id;
    private Pedido pedido;

    @BeforeEach //executa antes de cada méto.do de teste
    void prepararDados() {
        id = UUID.randomUUID();

        pedido = new Pedido();
        pedido.setId(id);
        pedido.setCliente("Carlos");
        pedido.setDescricao("Hamburguer");
        pedido.setStatus(StatusPedido.RECEBIDO);
    }

    @Nested
    class BuscaTests{
        @Test
        void deveLancarExcecaoQuandoPedidoNaoExistir() {
            UUID id = UUID.randomUUID();
            when(repository.findById(id)).thenReturn(Optional.empty());
            assertThrows(PedidoNaoEncontradoException.class, () -> service.buscarPorId(id));
        }

        @Test
        void deveRetornarPedidoQuandoExistir() {
            when(repository.findById(id)).thenReturn(Optional.of(pedido));

            Pedido resultado = service.buscarPorId(id);

            assertEquals(id, resultado.getId());
            assertEquals("Carlos", resultado.getCliente());
            assertEquals("Hamburguer", resultado.getDescricao());
            assertEquals(StatusPedido.RECEBIDO, resultado.getStatus());

            verify(repository, times(1)).findById(id);
            //verifique se o metodo findById do repository foi utilizado ao menos 1 vez
        }
    }

    @Nested
    class CadastroTests{
        @Test
        void deveCadastrarPedidoComStatusRecebido() {

            UUID produtoId = UUID.randomUUID();

            PedidoRequestDTO dto = new PedidoRequestDTO(
                    "Maria",
                    "X-salada",
                    List.of(new ItemPedidoRequestDTO(produtoId, 2))
            );

            Produto produto = new Produto();
            produto.setId(produtoId);
            produto.setNome("X-salada");
            produto.setPreco(new BigDecimal("25.00"));
            produto.setAtivo(true);

            when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produto));

            when(repository.save(any(Pedido.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            //devolve o mesmo objeto que foi enviado

            Pedido resultado = service.cadastrar(dto);

            assertNull(resultado.getId());
            assertEquals("Maria", resultado.getCliente());
            assertEquals("X-salada", resultado.getDescricao());
            assertEquals(StatusPedido.RECEBIDO, resultado.getStatus());

            assertEquals(1, resultado.getItens().size());

            verify(produtoRepository).findById(produtoId);
            verify(repository).save(any(Pedido.class));
        }

        @Test
        void deveEnviarPedidoCorretoParaORepository() {

            UUID produtoId = UUID.randomUUID();

            PedidoRequestDTO dto = new PedidoRequestDTO(
                    "Carlos",
                    "Pizza",
                    List.of(new ItemPedidoRequestDTO(produtoId, 2))
            );

            Produto produto = new Produto();
            produto.setId(produtoId);
            produto.setNome("Pizza");
            produto.setPreco(new BigDecimal("45.00"));
            produto.setAtivo(true);

            when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produto));

            ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);

            service.cadastrar(dto);

            verify(repository).save(captor.capture());

            Pedido pedidoCapturado = captor.getValue();

            assertNull(pedidoCapturado.getId());
            assertEquals("Carlos", pedidoCapturado.getCliente());
            assertEquals("Pizza", pedidoCapturado.getDescricao());
            assertEquals(StatusPedido.RECEBIDO, pedidoCapturado.getStatus() );

            assertEquals(1, pedidoCapturado.getItens().size());

            ItemPedido itemCapturado = pedidoCapturado.getItens().get(0);

            assertSame(pedidoCapturado, itemCapturado.getPedido());
            assertSame(produto, itemCapturado.getProduto());
            assertEquals(2, itemCapturado.getQuantidade());
            assertEquals(new BigDecimal("45.00"), itemCapturado.getPrecoUnitatio());
        }

        @Test
        void deveCadastrarPedidoComTresItens() {

            //Id dos 3 produtos
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            UUID id3 = UUID.randomUUID();

            //Produtos simulando registros existentes no banco
            Produto produto1 = new Produto();
            produto1.setId(UUID.randomUUID());
            produto1.setNome("Hamburguer");
            produto1.setPreco(new BigDecimal("25.00"));
            produto1.setAtivo(true);

            Produto produto2 = new Produto();
            produto2.setId(UUID.randomUUID());
            produto2.setNome("Batata");
            produto2.setPreco(new BigDecimal("15.00"));
            produto2.setAtivo(true);

            Produto produto3 = new Produto();
            produto3.setId(UUID.randomUUID());
            produto3.setNome("Refri");
            produto3.setPreco(new BigDecimal("10.00"));
            produto3.setAtivo(true);

            //DTO contendo 3 itens diferentes
            PedidoRequestDTO dto = new PedidoRequestDTO(
                    "Carlos",
                    "Combo completo",
                    List.of(new ItemPedidoRequestDTO(id1, 2),
                            new ItemPedidoRequestDTO(id2, 1),
                            new ItemPedidoRequestDTO(id3, 3))
            );

            //Configurando retorno do ProdutoRepository
            when(produtoRepository.findById(id1)).thenReturn(Optional.of(produto1));
            when(produtoRepository.findById(id2)).thenReturn(Optional.of(produto2));
            when(produtoRepository.findById(id3)).thenReturn(Optional.of(produto3));

            //Simulando o save do PedidoRepository
            when(repository.save(any(Pedido.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            //Executando o métod0 que queremos testar
            Pedido result = service.cadastrar(dto);

            //Verificando as propriedades do pedido
            assertEquals("Carlos", result.getCliente());
            assertEquals("Combo completo", result.getDescricao());
            assertEquals(StatusPedido.RECEBIDO, result.getStatus());

            //Verificando se os tres itens foram adicionados
            assertEquals(3, result.getItens().size());

            //Verificando os produtos de cada item
            assertSame(produto1, result.getItens().get(0).getProduto());
            assertSame(produto2, result.getItens().get(1).getProduto());
            assertSame(produto3, result.getItens().get(2).getProduto());

            //Verificando as quantidades
            assertEquals(2, result.getItens().get(0).getQuantidade());
            assertEquals(1, result.getItens().get(1).getQuantidade());
            assertEquals(3, result.getItens().get(2).getQuantidade());

            //Verificando os preços unitários
            assertEquals(new BigDecimal("25.00"), result.getItens().get(0).getPrecoUnitatio());
            assertEquals(new BigDecimal("15.00"), result.getItens().get(1).getPrecoUnitatio());
            assertEquals(new BigDecimal("10.00"), result.getItens().get(2).getPrecoUnitatio());

            //Verificando se todos os itens pertencem ao mesmo pedido
            for(ItemPedido item : result.getItens()) {
                assertSame(result, item.getPedido());
            }

            //Verificando as interações com os repositórios
            verify(produtoRepository).findById(id1);
            verify(produtoRepository).findById(id2);
            verify(produtoRepository).findById(id3);

            verify(repository, times(1)).save(result);
        }
    }

    @Nested
    class AtualizacaoStatusTests{
        @Test
        void deveAtualizarStatusQuandoTransicaoForPermitida() {

            AtualizarStatusPedidoDTO dto = new AtualizarStatusPedidoDTO(StatusPedido.EM_PREPARACAO);

            when(repository.findById(id)).thenReturn(Optional.of(pedido));

            when(repository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Pedido result = service.atualizarStatus(id, dto);

            assertEquals(StatusPedido.EM_PREPARACAO, result.getStatus());

            verify(repository).findById(id);
            verify(repository).save(pedido);
        }

        @Test
        void deveLancarExcecaoQuandoTransicaoForProibida() {

            AtualizarStatusPedidoDTO dto = new AtualizarStatusPedidoDTO(StatusPedido.PRONTO);

            when(repository.findById(id)).thenReturn(Optional.of(pedido));

            assertThrows(PedidoStatusInvalidoException.class, () -> service.atualizarStatus(id, dto));

            assertEquals(StatusPedido.RECEBIDO, pedido.getStatus());

            verify(repository).findById(id);
            verify(repository, never()).save(any(Pedido.class));
        }
    }
}


