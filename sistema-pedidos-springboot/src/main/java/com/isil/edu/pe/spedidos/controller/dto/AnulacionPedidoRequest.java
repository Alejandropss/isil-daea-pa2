package com.isil.edu.pe.spedidos.controller.dto;

/**
 * Cuerpo de POST /api/v1/anulaciones. Ejemplo:
 * { "pedidoId": 1, "motivo": "Solicitud del cliente" }
 */
public record AnulacionPedidoRequest(Long pedidoId, String motivo) {
}
