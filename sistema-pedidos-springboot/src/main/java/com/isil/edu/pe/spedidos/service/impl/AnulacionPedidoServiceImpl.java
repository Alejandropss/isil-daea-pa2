package com.isil.edu.pe.spedidos.service.impl;

import com.isil.edu.pe.spedidos.client.DevolucionStockClient;
import com.isil.edu.pe.spedidos.client.InventarioClientException;
import com.isil.edu.pe.spedidos.client.InventarioNoDisponibleException;
import com.isil.edu.pe.spedidos.client.dto.DevolucionStockResponse;
import com.isil.edu.pe.spedidos.entity.AnulacionPedido;
import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.entity.PedidoEstado;
import com.isil.edu.pe.spedidos.exception.AnulacionNoPermitidaException;
import com.isil.edu.pe.spedidos.exception.PedidoException;
import com.isil.edu.pe.spedidos.exception.PedidoNoEncontradoException;
import com.isil.edu.pe.spedidos.repository.AnulacionPedidoRepository;
import com.isil.edu.pe.spedidos.repository.PedidoRepository;
import com.isil.edu.pe.spedidos.service.AnulacionPedidoService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Anula pedidos coordinando Inventario (remoto) y H2 (local).
 *
 * <p>Las tres dependencias llegan por Constructor Injection: Spring crea este
 * bean pasándole los repositorios y el cliente HTTP. En las pruebas unitarias se
 * construye con mocks, sin levantar Spring ni inventario-service.</p>
 */
@Service
public class AnulacionPedidoServiceImpl implements AnulacionPedidoService {

  private static final Logger log = LoggerFactory.getLogger(AnulacionPedidoServiceImpl.class);
  private static final int MOTIVO_MAXIMO = 200;

  private final PedidoRepository pedidoRepository;
  private final AnulacionPedidoRepository anulacionPedidoRepository;
  private final DevolucionStockClient devolucionStockClient;

  public AnulacionPedidoServiceImpl(
      PedidoRepository pedidoRepository,
      AnulacionPedidoRepository anulacionPedidoRepository,
      DevolucionStockClient devolucionStockClient) {
    this.pedidoRepository = pedidoRepository;
    this.anulacionPedidoRepository = anulacionPedidoRepository;
    this.devolucionStockClient = devolucionStockClient;
  }

  /**
   * Orden deliberado: 1) validaciones locales, 2) devolución remota, 3) escritura en H2.
   *
   * <p>@Transactional solo controla H2. Si Inventario falla en el paso 2, la excepción
   * sale antes del paso 3 y no queda ninguna anulación local. La devolución remota y
   * la escritura local NO forman una transacción distribuida atómica.</p>
   */
  @Override
  @Transactional
  public AnulacionPedido anularPedido(Long pedidoId, String motivo) {
    if (pedidoId == null || pedidoId <= 0) {
      throw new PedidoException("El pedidoId es obligatorio y debe ser mayor que cero.");
    }
    String motivoNormalizado = normalizarMotivo(motivo);

    Pedido pedido = pedidoRepository.findById(pedidoId)
        .orElseThrow(() -> new PedidoNoEncontradoException(pedidoId));

    // Duplicados: se rechazan ANTES de llamar a Inventario, así no se devuelve stock dos veces.
    if (pedido.getEstado() != PedidoEstado.REGISTRADO || anulacionPedidoRepository.existsByPedidoId(pedidoId)) {
      throw new AnulacionNoPermitidaException(
          "El pedido " + pedidoId + " ya fue anulado (estado " + pedido.getEstado()
              + "); no se devolverá stock nuevamente.");
    }

    DevolucionStockResponse devolucion = solicitarDevolucion(pedido);

    pedido.anular();
    pedidoRepository.save(pedido);
    AnulacionPedido anulacion = anulacionPedidoRepository.save(new AnulacionPedido(
        pedidoId, pedido.getProductoCodigo(), devolucion.getCantidadDevuelta(), motivoNormalizado));

    log.info("Pedido {} anulado: {} unidad(es) de {} devueltas; stock en Inventario: {}.",
        pedidoId, devolucion.getCantidadDevuelta(), devolucion.getCodigo(), devolucion.getStockRestante());
    return anulacion;
  }

  @Override
  @Transactional(readOnly = true)
  public List<AnulacionPedido> listarAnulaciones() {
    return anulacionPedidoRepository.findAllByOrderByIdDesc();
  }

  private DevolucionStockResponse solicitarDevolucion(Pedido pedido) {
    try {
      return devolucionStockClient.devolverStock(pedido.getProductoCodigo(), pedido.getCantidad());
    } catch (InventarioClientException e) {
      // Un 5xx es una falla técnica del servicio remoto, no un error de los datos enviados.
      if (e.getStatus() >= 500) {
        throw new InventarioNoDisponibleException(
            "El servicio de inventario presentó un error temporal (HTTP " + e.getStatus() + ").", e);
      }
      throw e;
    }
  }

  private static String normalizarMotivo(String motivo) {
    if (motivo == null || motivo.isBlank()) {
      return null;
    }
    String limpio = motivo.trim();
    if (limpio.length() > MOTIVO_MAXIMO) {
      throw new PedidoException("El motivo no puede superar " + MOTIVO_MAXIMO + " caracteres.");
    }
    return limpio;
  }
}
