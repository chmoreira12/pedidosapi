package com.lanchonete.pedidosapi.controller;

import com.lanchonete.pedidosapi.dto.AtualizarStatusPedidoDTO;
import com.lanchonete.pedidosapi.dto.PedidoRequestDTO;
import com.lanchonete.pedidosapi.dto.PedidoResponseDTO;
import com.lanchonete.pedidosapi.mapper.PedidoMapper;
import com.lanchonete.pedidosapi.model.Pedido;
import com.lanchonete.pedidosapi.model.StatusPedido;
import com.lanchonete.pedidosapi.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }


    @GetMapping
    public ResponseEntity<List<PedidoResponseDTO>> listarTodos() {
        List<PedidoResponseDTO> pedidos = pedidoService.listarTodos()
                .stream()
                .map(PedidoMapper::toResponseDTO)
                .toList();

        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/{id}")
    public Pedido buscarPorID(@PathVariable UUID id) {
        return pedidoService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<PedidoResponseDTO> cadastrar(@Valid @RequestBody PedidoRequestDTO dto) {

        Pedido pedido = pedidoService.cadastrar(dto);

        PedidoResponseDTO response = PedidoMapper.toResponseDTO(pedido);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PedidoResponseDTO> atualizarStatus(
            @PathVariable UUID id, @Valid @RequestBody AtualizarStatusPedidoDTO dto) {

        Pedido pedido = pedidoService.atualizarStatus(id, dto);

        return ResponseEntity.ok(PedidoMapper.toResponseDTO(pedido));

    }

    @GetMapping("/status/{status}")
    public Page<PedidoResponseDTO> buscarPorStatusPaginado(@PathVariable StatusPedido status, Pageable pageable) {
        return pedidoService.buscarPorStatusPaginado(status, pageable)
                .map(PedidoMapper::toResponseDTO);
    }
}
