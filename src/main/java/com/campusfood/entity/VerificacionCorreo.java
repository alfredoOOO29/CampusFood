package com.campusfood.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "verificaciones_correo")
public class VerificacionCorreo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 6)
    private String codigo;

    @Column(name = "token")
    private String token;

    @OneToOne(targetEntity = Usuario.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime fechaExpiracion;

    public VerificacionCorreo() {
    }

    public VerificacionCorreo(String codigo, String token, Usuario usuario, LocalDateTime fechaExpiracion) {
        this.codigo = codigo;
        this.token = token;
        this.usuario = usuario;
        this.fechaExpiracion = fechaExpiracion;
    }

    public VerificacionCorreo(String codigo, Usuario usuario, LocalDateTime fechaExpiracion) {
        this.codigo = codigo;
        this.usuario = usuario;
        this.fechaExpiracion = fechaExpiracion;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public LocalDateTime getFechaExpiracion() { return fechaExpiracion; }
    public void setFechaExpiracion(LocalDateTime fechaExpiracion) { this.fechaExpiracion = fechaExpiracion; }
}
