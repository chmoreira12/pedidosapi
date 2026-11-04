package com.lanchonete.pedidosapi.repository;

import com.lanchonete.pedidosapi.model.Pedido;
import com.lanchonete.pedidosapi.model.StatusPedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PedidoRepository extends JpaRepository<Pedido, UUID> {

    List<Pedido> findByCliente(String cliente);

    @Query("SELECT p FROM Pedido p WHERE p.status = :status")
    List<Pedido> buscarPorStatus(@Param("status")StatusPedido status);

    @Query("SELECT p FROM Pedido p WHERE p.cliente = :cliente AND p.status = :status")
    List<Pedido> buscarPorClienteEStatus(@Param("cliente") String cliente, @Param("status") StatusPedido status);

    @Query("SELECT p FROM Pedido p WHERE p.cliente = :cliente AND (p.status = :status1 OR p.status = :status2)")
    List<Pedido> buscarPorClienteEDoisStatus(
            @Param("cliente") String cliente,
            @Param("status1") StatusPedido status1,
            @Param("status2") StatusPedido status2);

    @Query("SELECT p FROM Pedido p WHERE p.cliente = :cliente AND p.status IN :status")
    List<Pedido> buscarPorClienteEStatus(@Param("cliente") String cliente, @Param("status") List<StatusPedido> status);

    @Query("SELECT p FROM Pedido p ORDER BY p.cliente ASC")
    List<Pedido> buscarTodosOrdenadoPorCliente();

    @Query("SELECT p FROM Pedido p WHERE p.status = :status")
    Page<Pedido> buscarPorStatusPaginado(@Param("status") StatusPedido status, Pageable pageable);

    @Query("SELECT p FROM Pedido p WHERE p.status = :status")
    Slice<Pedido> buscarPorStatusEmSlice(@Param("status") StatusPedido status, Pageable pageable);
}
