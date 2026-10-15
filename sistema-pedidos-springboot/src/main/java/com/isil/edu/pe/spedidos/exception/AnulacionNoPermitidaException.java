package com.isil.edu.pe.spedidos.exception;

/**
 * El pedido ya fue anulado o no está en estado REGISTRADO (se responde 409).
 */
public class AnulacionNoPermitidaException extends PedidoException {

  public AnulacionNoPermitidaException(String message) {
    super(message);
  }
}
