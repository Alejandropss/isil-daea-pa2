package com.isil.edu.pe.spedidos.controller.dto;

import com.isil.edu.pe.spedidos.entity.AnulacionPedido;
import com.isil.edu.pe.spedidos.entity.PedidoEstado;
import java.time.LocalDateTime;

/**
 * DTO de salida: la entidad JPA nunca se expone directamente como contrato JSON.
 */
public record AnulacionPedidoResponse(
    Long id,
    Long pedidoId,
    String productoCodigo,
    int cantidadDevuelta,
    String motivo,
    LocalDateTime fecha,
    PedidoEstado estadoPedido
) {

  public static AnulacionPedidoResponse from(AnulacionPedido anulacion) {
    return new AnulacionPedidoResponse(
        anulacion.getId(),
        anulacion.getPedidoId(),
        anulacion.getProductoCodigo(),
        anulacion.getCantidadDevuelta(),
        anulacion.getMotivo(),
        anulacion.getFecha(),
        // Solo existe un registro de anulación para pedidos ya ANULADOS.
        PedidoEstado.ANULADO
    );
  }
}
