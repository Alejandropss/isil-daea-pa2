package com.isil.edu.pe.spedidos.exception;

/**
 * El pedido solicitado no existe en H2 (se responde 404).
 */
public class PedidoNoEncontradoException extends PedidoException {

  public PedidoNoEncontradoException(Long pedidoId) {
    super("No existe un pedido con id " + pedidoId + ".");
  }
}
