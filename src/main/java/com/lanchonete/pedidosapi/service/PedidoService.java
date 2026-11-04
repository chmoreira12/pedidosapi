package com.lanchonete.pedidosapi.service;

import com.lanchonete.pedidosapi.dto.AtualizarStatusPedidoDTO;
import com.lanchonete.pedidosapi.dto.PedidoRequestDTO;
import com.lanchonete.pedidosapi.exception.PedidoNaoEncontradoException;
import com.lanchonete.pedidosapi.exception.PedidoStatusInvalidoException;
import com.lanchonete.pedidosapi.model.Pedido;
import com.lanchonete.pedidosapi.model.StatusPedido;
import com.lanchonete.pedidosapi.repository.PedidoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;

    public PedidoService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    public Pedido buscarPorId(UUID id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new PedidoNaoEncontradoException("Pedido não encontrado!"));
    }

    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }

    public Pedido cadastrar(PedidoRequestDTO dto) {
        Pedido pedido = new Pedido();

        pedido.setId(UUID.randomUUID());
        pedido.setCliente(dto.cliente());
        pedido.setDescricao(dto.descricao());
        pedido.setStatus(StatusPedido.RECEBIDO);

        return pedidoRepository.save(pedido);
    }

    public Pedido atualizarStatus(UUID id, AtualizarStatusPedidoDTO dto) {

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new PedidoNaoEncontradoException("Pedido não encontrado!"));

        StatusPedido atual = pedido.getStatus();
        StatusPedido novo = dto.status();

        if(!atual.podeTransicionarpara(novo)) {
            throw new PedidoStatusInvalidoException("Transição não permitida!");
        }

        pedido.setStatus(novo);

        return pedidoRepository.save(pedido);

    }

    public Page<Pedido> buscarPorStatusPaginado(StatusPedido status, Pageable pageable) {
        return pedidoRepository.buscarPorStatusPaginado(status, pageable);
    }

}
