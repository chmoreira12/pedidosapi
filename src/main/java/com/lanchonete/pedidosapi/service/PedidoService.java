package com.lanchonete.pedidosapi.service;

import com.lanchonete.pedidosapi.dto.AtualizarStatusPedidoDTO;
import com.lanchonete.pedidosapi.dto.ItemPedidoRequestDTO;
import com.lanchonete.pedidosapi.dto.PedidoRequestDTO;
import com.lanchonete.pedidosapi.exception.PedidoNaoEncontradoException;
import com.lanchonete.pedidosapi.exception.PedidoStatusInvalidoException;
import com.lanchonete.pedidosapi.model.ItemPedido;
import com.lanchonete.pedidosapi.model.Pedido;
import com.lanchonete.pedidosapi.model.Produto;
import com.lanchonete.pedidosapi.model.StatusPedido;
import com.lanchonete.pedidosapi.repository.PedidoRepository;
import com.lanchonete.pedidosapi.repository.ProdutoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;

    public PedidoService(PedidoRepository pedidoRepository, ProdutoRepository produtoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
    }

    public Pedido buscarPorId(UUID id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new PedidoNaoEncontradoException("Pedido não encontrado!"));
    }

    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }

    @Transactional
    public Pedido cadastrar(PedidoRequestDTO dto) {

        Pedido pedido = new Pedido();
        pedido.setCliente(dto.cliente());
        pedido.setDescricao(dto.descricao());
        pedido.setStatus(StatusPedido.RECEBIDO);

        for (ItemPedidoRequestDTO itens : dto.item()) {
            Produto produto = produtoRepository.findById(itens.id())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado!"));

            ItemPedido itemPedido = new ItemPedido();
            itemPedido.setPedido(pedido);
            itemPedido.setProduto(produto);
            itemPedido.setQuantidade(itens.quantidade());
            itemPedido.setPrecoUnitatio(produto.getPreco());

            pedido.getItens().add(itemPedido);
        }
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
