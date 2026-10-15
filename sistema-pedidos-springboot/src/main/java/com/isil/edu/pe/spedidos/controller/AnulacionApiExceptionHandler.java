package com.isil.edu.pe.spedidos.controller;

import com.isil.edu.pe.spedidos.client.InventarioClientException;
import com.isil.edu.pe.spedidos.client.InventarioNoDisponibleException;
import com.isil.edu.pe.spedidos.controller.dto.ApiErrorResponse;
import com.isil.edu.pe.spedidos.exception.AnulacionNoPermitidaException;
import com.isil.edu.pe.spedidos.exception.PedidoException;
import com.isil.edu.pe.spedidos.exception.PedidoNoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Convierte las excepciones de la API de anulaciones en respuestas JSON con un
 * código HTTP coherente. Solo aplica a AnulacionPedidoController, para no
 * alterar el comportamiento de las vistas JSP existentes.
 */
@RestControllerAdvice(assignableTypes = AnulacionPedidoController.class)
public class AnulacionApiExceptionHandler {

  @ExceptionHandler(PedidoNoEncontradoException.class)
  public ResponseEntity<ApiErrorResponse> pedidoNoEncontrado(PedidoNoEncontradoException e, HttpServletRequest req) {
    return responder(HttpStatus.NOT_FOUND, e.getMessage(), req);
  }

  @ExceptionHandler(AnulacionNoPermitidaException.class)
  public ResponseEntity<ApiErrorResponse> duplicada(AnulacionNoPermitidaException e, HttpServletRequest req) {
    return responder(HttpStatus.CONFLICT, e.getMessage(), req);
  }

  /** Red de seguridad: la restricción UNIQUE de pedido_id rechazó una segunda anulación. */
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiErrorResponse> violacionUnique(DataIntegrityViolationException e, HttpServletRequest req) {
    return responder(HttpStatus.CONFLICT, "Ya existe una anulación registrada para este pedido.", req);
  }

  @ExceptionHandler(InventarioNoDisponibleException.class)
  public ResponseEntity<ApiErrorResponse> inventarioNoDisponible(InventarioNoDisponibleException e,
      HttpServletRequest req) {
    return responder(HttpStatus.SERVICE_UNAVAILABLE,
        e.getMessage() + " El pedido conserva el estado REGISTRADO.", req);
  }

  /** Inventario respondió, pero rechazó la devolución (4xx). */
  @ExceptionHandler(InventarioClientException.class)
  public ResponseEntity<ApiErrorResponse> inventarioRechazo(InventarioClientException e, HttpServletRequest req) {
    return responder(HttpStatus.BAD_GATEWAY,
        "inventario-service rechazó la devolución (HTTP " + e.getStatus() + "): " + e.getMessage(), req);
  }

  @ExceptionHandler(PedidoException.class)
  public ResponseEntity<ApiErrorResponse> datosInvalidos(PedidoException e, HttpServletRequest req) {
    return responder(HttpStatus.BAD_REQUEST, e.getMessage(), req);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiErrorResponse> jsonInvalido(HttpMessageNotReadableException e, HttpServletRequest req) {
    return responder(HttpStatus.BAD_REQUEST, "El cuerpo JSON es inválido o está vacío.", req);
  }

  private static ResponseEntity<ApiErrorResponse> responder(HttpStatus status, String mensaje, HttpServletRequest req) {
    return ResponseEntity.status(status)
        .body(ApiErrorResponse.of(status.value(), status.getReasonPhrase(), mensaje, req.getRequestURI()));
  }
}
