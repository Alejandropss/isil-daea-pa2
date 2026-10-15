package com.isil.edu.pe.spedidos.controller;

import com.isil.edu.pe.spedidos.controller.dto.AnulacionPedidoRequest;
import com.isil.edu.pe.spedidos.controller.dto.AnulacionPedidoResponse;
import com.isil.edu.pe.spedidos.entity.AnulacionPedido;
import com.isil.edu.pe.spedidos.service.AnulacionPedidoService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API REST de anulaciones. Solo traduce HTTP/JSON y delega en el Service,
 * que recibe por Constructor Injection.
 */
@RestController
@RequestMapping("/api/v1/anulaciones")
public class AnulacionPedidoController {

  private final AnulacionPedidoService anulacionPedidoService;

  public AnulacionPedidoController(AnulacionPedidoService anulacionPedidoService) {
    this.anulacionPedidoService = anulacionPedidoService;
  }

  @PostMapping
  public ResponseEntity<AnulacionPedidoResponse> anular(@RequestBody AnulacionPedidoRequest request) {
    AnulacionPedido anulacion = anulacionPedidoService.anularPedido(request.pedidoId(), request.motivo());
    return ResponseEntity.status(HttpStatus.CREATED).body(AnulacionPedidoResponse.from(anulacion));
  }

  @GetMapping
  public List<AnulacionPedidoResponse> listar() {
    return anulacionPedidoService.listarAnulaciones()
        .stream()
        .map(AnulacionPedidoResponse::from)
        .toList();
  }
}
