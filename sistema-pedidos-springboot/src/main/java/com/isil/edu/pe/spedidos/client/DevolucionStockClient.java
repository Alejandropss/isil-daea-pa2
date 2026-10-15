package com.isil.edu.pe.spedidos.client;

import com.isil.edu.pe.spedidos.client.dto.DevolucionStockResponse;

/**
 * Contrato para devolver stock al servicio externo de Inventario.
 *
 * <p>La capa de negocio depende de esta interfaz y no de su implementación HTTP:
 * así puede reemplazarse por un mock en las pruebas unitarias.</p>
 */
public interface DevolucionStockClient {

  /**
   * Solicita a Inventario devolver unidades de un producto.
   *
   * @throws InventarioClientException si Inventario responde 4xx/5xx
   * @throws InventarioNoDisponibleException si no hay conexión o se supera el timeout
   */
  DevolucionStockResponse devolverStock(String codigo, int cantidad);
}
