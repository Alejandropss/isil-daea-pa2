package com.isil.edu.pe.spedidos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/** Reto: se guarda cuando la compensación falla, para revisar el stock después. */
@Entity
@Table(name = "incidente_conciliacion")
public class IncidenteConciliacion {

  public static final String PENDIENTE_CONCILIACION = "PENDIENTE_CONCILIACION";
  private static final int DETALLE_MAXIMO = 500;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 40)
  private String operacion;

  @Column(name = "producto_codigo", nullable = false, length = 30)
  private String productoCodigo;

  @Column(nullable = false)
  private int cantidad;

  @Column(nullable = false, length = DETALLE_MAXIMO)
  private String detalle;

  @Column(nullable = false, length = 30)
  private String estado;

  @Column(nullable = false)
  private LocalDateTime fecha;

  protected IncidenteConciliacion() {
  }

  public IncidenteConciliacion(String operacion, String productoCodigo, int cantidad, String detalle) {
    this.operacion = operacion;
    this.productoCodigo = productoCodigo;
    this.cantidad = cantidad;
    this.detalle = detalle.length() > DETALLE_MAXIMO ? detalle.substring(0, DETALLE_MAXIMO) : detalle;
    this.estado = PENDIENTE_CONCILIACION;
    this.fecha = LocalDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public String getOperacion() {
    return operacion;
  }

  public String getProductoCodigo() {
    return productoCodigo;
  }

  public int getCantidad() {
    return cantidad;
  }

  public String getDetalle() {
    return detalle;
  }

  public String getEstado() {
    return estado;
  }

  public LocalDateTime getFecha() {
    return fecha;
  }
}
