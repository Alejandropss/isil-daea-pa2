package com.isil.edu.pe.spedidos.controller.dto;

import java.time.OffsetDateTime;

/**
 * Estructura JSON de error de la API de anulaciones.
 */
public record ApiErrorResponse(int status, String error, String message, String path, String timestamp) {

  public static ApiErrorResponse of(int status, String error, String message, String path) {
    return new ApiErrorResponse(status, error, message, path, OffsetDateTime.now().toString());
  }
}
