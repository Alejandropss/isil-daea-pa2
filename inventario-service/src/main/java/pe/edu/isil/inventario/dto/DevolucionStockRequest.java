package pe.edu.isil.inventario.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Solicitud de POST /inventario/productos/codigo/{codigo}/devoluciones.
 *
 * <p>Tiene la misma forma que {@link ReservaStockRequest}, pero se mantiene como
 * contrato propio: reservar y devolver son operaciones distintas y cada una
 * puede evolucionar sin afectar a la otra.</p>
 */
public class DevolucionStockRequest {

  @NotNull(message = "La cantidad es obligatoria.")
  @Positive(message = "La cantidad debe ser mayor que cero.")
  private Integer cantidad;

  public DevolucionStockRequest() {
  }

  public Integer getCantidad() {
    return cantidad;
  }

  public void setCantidad(Integer cantidad) {
    this.cantidad = cantidad;
  }
}
