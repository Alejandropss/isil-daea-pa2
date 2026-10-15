package pe.edu.isil.inventario.dto;

/**
 * Respuesta 200 de una devolución de stock.
 */
public class DevolucionStockResponse {

  private Long productoId;
  private String codigo;
  private String nombre;
  private int cantidadDevuelta;
  private int stockRestante;
  private Long version;

  public DevolucionStockResponse() {
  }

  public DevolucionStockResponse(
      Long productoId,
      String codigo,
      String nombre,
      int cantidadDevuelta,
      int stockRestante,
      Long version
  ) {
    this.productoId = productoId;
    this.codigo = codigo;
    this.nombre = nombre;
    this.cantidadDevuelta = cantidadDevuelta;
    this.stockRestante = stockRestante;
    this.version = version;
  }

  public Long getProductoId() {
    return productoId;
  }

  public String getCodigo() {
    return codigo;
  }

  public String getNombre() {
    return nombre;
  }

  public int getCantidadDevuelta() {
    return cantidadDevuelta;
  }

  public int getStockRestante() {
    return stockRestante;
  }

  public Long getVersion() {
    return version;
  }
}
