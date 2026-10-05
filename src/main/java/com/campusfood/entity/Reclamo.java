package com.campusfood.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reclamos")
public class Reclamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reportante_id", nullable = false)
    private Usuario reportante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reportado_id", nullable = false)
    private Usuario reportado;

    @Column(nullable = false, length = 1000)
    private String motivo;

    @Column(name = "evidencia_url", length = 500)
    private String evidenciaUrl; // Para links a imágenes, etc.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoReclamo estado = EstadoReclamo.PENDIENTE;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
    
    // Si fue apelado
    @Column(name = "motivo_apelacion", length = 1000)
    private String motivoApelacion;

    public Reclamo() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Pedido getPedido() { return pedido; }
    public void setPedido(Pedido pedido) { this.pedido = pedido; }
    public Usuario getReportante() { return reportante; }
    public void setReportante(Usuario reportante) { this.reportante = reportante; }
    public Usuario getReportado() { return reportado; }
    public void setReportado(Usuario reportado) { this.reportado = reportado; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getEvidenciaUrl() { return evidenciaUrl; }
    public void setEvidenciaUrl(String evidenciaUrl) { this.evidenciaUrl = evidenciaUrl; }
    public EstadoReclamo getEstado() { return estado; }
    public void setEstado(EstadoReclamo estado) { this.estado = estado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public String getMotivoApelacion() { return motivoApelacion; }
    public void setMotivoApelacion(String motivoApelacion) { this.motivoApelacion = motivoApelacion; }
}
