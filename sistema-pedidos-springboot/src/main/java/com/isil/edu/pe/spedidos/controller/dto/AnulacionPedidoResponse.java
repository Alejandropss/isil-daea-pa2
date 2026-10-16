package com.isil.edu.pe.spedidos.controller.dto;

import com.isil.edu.pe.spedidos.entity.AnulacionPedido;
import com.isil.edu.pe.spedidos.entity.PedidoEstado;
import java.time.LocalDateTime;

/** Respuesta JSON de una anulación (no se devuelve la entidad directamente). */
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
