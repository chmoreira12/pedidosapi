package com.lanchonete.pedidosapi.service;

import com.lanchonete.pedidosapi.dto.AtualizarStatusPedidoDTO;
import com.lanchonete.pedidosapi.dto.PedidoRequestDTO;
import com.lanchonete.pedidosapi.dto.PedidoResponseDTO;
import com.lanchonete.pedidosapi.exception.PedidoNaoEncontradoException;
import com.lanchonete.pedidosapi.exception.PedidoStatusInvalidoException;
import com.lanchonete.pedidosapi.model.Pedido;
import com.lanchonete.pedidosapi.model.StatusPedido;
import com.lanchonete.pedidosapi.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.engine.TestExecutionResult;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
            PedidoRequestDTO dto = new PedidoRequestDTO(
                    "Maria",
                    "X-Salada"
            );

            when(repository.save(any(Pedido.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            //devolve o mesmo objeto que foi enviado

            Pedido resultado = service.cadastrar(dto);

            assertNotNull(resultado.getId());
            assertEquals("Maria", resultado.getCliente());
            assertEquals("X-Salada", resultado.getDescricao());
            assertEquals(StatusPedido.RECEBIDO, resultado.getStatus());

            verify(repository, times(1)).save(any(Pedido.class));
        }

        @Test
        void deveEnviarPedidoCorretoParaORepository() {
            PedidoRequestDTO dto = new PedidoRequestDTO(
                    "Carlos",
                    "Pizza"
            );

            ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);

            service.cadastrar(dto);

            verify(repository).save(captor.capture());

            Pedido pedidoCapturado = captor.getValue();

            assertNotNull(pedidoCapturado.getId());
            assertEquals("Carlos", pedidoCapturado.getCliente());
            assertEquals("Pizza", pedidoCapturado.getDescricao());
            assertEquals(StatusPedido.RECEBIDO, pedidoCapturado.getStatus() );
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


