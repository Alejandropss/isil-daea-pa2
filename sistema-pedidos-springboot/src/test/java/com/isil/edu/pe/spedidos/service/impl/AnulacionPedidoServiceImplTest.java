package com.isil.edu.pe.spedidos.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.isil.edu.pe.spedidos.client.DevolucionStockClient;
import com.isil.edu.pe.spedidos.client.InventarioNoDisponibleException;
import com.isil.edu.pe.spedidos.client.dto.DevolucionStockResponse;
import com.isil.edu.pe.spedidos.entity.AnulacionPedido;
import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.entity.PedidoEstado;
import com.isil.edu.pe.spedidos.exception.AnulacionNoPermitidaException;
import com.isil.edu.pe.spedidos.repository.AnulacionPedidoRepository;
import com.isil.edu.pe.spedidos.repository.PedidoRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * PA2 - Parte C: pruebas unitarias de AnulacionPedidoServiceImpl con Mockito.
 * Gracias a Constructor Injection el servicio se crea con "new" y mocks:
 * no se levanta Spring, ni H2, ni inventario-service.
 */
class AnulacionPedidoServiceImplTest {

  private PedidoRepository pedidoRepository;
  private AnulacionPedidoRepository anulacionPedidoRepository;
  private DevolucionStockClient devolucionStockClient;
  private AnulacionPedidoServiceImpl service;

  @BeforeEach
  void setUp() {
    pedidoRepository = mock(PedidoRepository.class);
    anulacionPedidoRepository = mock(AnulacionPedidoRepository.class);
    devolucionStockClient = mock(DevolucionStockClient.class);
    service = new AnulacionPedidoServiceImpl(pedidoRepository, anulacionPedidoRepository, devolucionStockClient);
  }

  @Test
  void anulacionCorrectaDevuelveStockUnaVezYRegistraLaAnulacion() {
    Pedido pedido = pedidoRegistrado(1L, "LAP-001", 2);
    when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
    when(anulacionPedidoRepository.existsByPedidoId(1L)).thenReturn(false);
    when(devolucionStockClient.devolverStock("LAP-001", 2)).thenReturn(devolucion("LAP-001", 2, 10));
    when(anulacionPedidoRepository.save(any(AnulacionPedido.class))).thenAnswer(inv -> inv.getArgument(0));

    AnulacionPedido anulacion = service.anularPedido(1L, "Solicitud del cliente");

    // Una sola devolución remota con el producto y la cantidad del pedido.
    verify(devolucionStockClient, times(1)).devolverStock("LAP-001", 2);
    // El pedido no se borra: cambia a ANULADO.
    assertEquals(PedidoEstado.ANULADO, pedido.getEstado());
    verify(pedidoRepository).save(pedido);

    ArgumentCaptor<AnulacionPedido> captor = ArgumentCaptor.forClass(AnulacionPedido.class);
    verify(anulacionPedidoRepository).save(captor.capture());
    assertEquals(1L, captor.getValue().getPedidoId());
    assertEquals("LAP-001", captor.getValue().getProductoCodigo());
    assertEquals(2, captor.getValue().getCantidadDevuelta());
    assertEquals("Solicitud del cliente", anulacion.getMotivo());
  }

  @Test
  void anulacionDuplicadaNoDevuelveStockNiCreaOtroRegistro() {
    Pedido pedido = pedidoRegistrado(1L, "LAP-001", 2);
    pedido.anular(); // ya fue anulado antes
    when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
    when(anulacionPedidoRepository.existsByPedidoId(1L)).thenReturn(true);

    assertThrows(AnulacionNoPermitidaException.class, () -> service.anularPedido(1L, "Otra vez"));

    verifyNoInteractions(devolucionStockClient);
    verify(anulacionPedidoRepository, never()).save(any());
    verify(pedidoRepository, never()).save(any());
  }

  @Test
  void falloDeInventarioNoGuardaNadaEnLaBaseLocal() {
    Pedido pedido = pedidoRegistrado(1L, "LAP-001", 2);
    when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
    when(anulacionPedidoRepository.existsByPedidoId(1L)).thenReturn(false);
    when(devolucionStockClient.devolverStock("LAP-001", 2))
        .thenThrow(new InventarioNoDisponibleException("El servicio de inventario no se encuentra disponible."));

    assertThrows(InventarioNoDisponibleException.class, () -> service.anularPedido(1L, "Solicitud del cliente"));

    // Ninguna escritura local después del error: el pedido sigue REGISTRADO.
    assertEquals(PedidoEstado.REGISTRADO, pedido.getEstado());
    verify(pedidoRepository, never()).save(any());
    verify(anulacionPedidoRepository, never()).save(any());
  }

  private static Pedido pedidoRegistrado(Long id, String codigo, int cantidad) {
    BigDecimal precio = new BigDecimal("2500.00");
    return new Pedido(id, "Ana Torres", codigo, "Laptop", precio, cantidad,
        precio.multiply(BigDecimal.valueOf(cantidad)), LocalDateTime.now());
  }

  private static DevolucionStockResponse devolucion(String codigo, int cantidad, int stockRestante) {
    DevolucionStockResponse respuesta = new DevolucionStockResponse();
    respuesta.setCodigo(codigo);
    respuesta.setCantidadDevuelta(cantidad);
    respuesta.setStockRestante(stockRestante);
    return respuesta;
  }
}
