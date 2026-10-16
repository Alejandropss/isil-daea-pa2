package com.isil.edu.pe.spedidos.service;

import com.isil.edu.pe.spedidos.entity.AnulacionPedido;
import java.util.List;

/**
 * Casos de uso de anulación de pedidos (PA2).
 */
public interface AnulacionPedidoService {

  /** Devuelve el stock, guarda la anulación y cambia el pedido a ANULADO. */
  AnulacionPedido anularPedido(Long pedidoId, String motivo);

  List<AnulacionPedido> listarAnulaciones();
}
