package com.isil.edu.pe.spedidos.client.dto;

/**
 * Contrato recibido de inventario-service después de devolver stock.
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

  public Long getProductoId() {
    return productoId;
  }

  public void setProductoId(Long productoId) {
    this.productoId = productoId;
  }

  public String getCodigo() {
    return codigo;
  }

  public void setCodigo(String codigo) {
    this.codigo = codigo;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public int getCantidadDevuelta() {
    return cantidadDevuelta;
  }

  public void setCantidadDevuelta(int cantidadDevuelta) {
    this.cantidadDevuelta = cantidadDevuelta;
  }

  public int getStockRestante() {
    return stockRestante;
  }

  public void setStockRestante(int stockRestante) {
    this.stockRestante = stockRestante;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }
}
