package com.isil.edu.pe.spedidos.client;

import com.isil.edu.pe.spedidos.client.dto.DevolucionStockResponse;

/** Interfaz para devolver stock a Inventario (en las pruebas se usa un mock). */
public interface DevolucionStockClient {

  /** Pide a Inventario devolver unidades de un producto. */
  DevolucionStockResponse devolverStock(String codigo, int cantidad);
}
