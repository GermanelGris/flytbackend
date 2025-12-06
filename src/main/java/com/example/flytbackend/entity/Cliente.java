package com.example.flytbackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "apellido", nullable = false, length = 100)
    private String apellido;

    @Column(name = "email", unique = true, nullable = false, length = 150)
    private String email;

    @Column(name = "telefono", length = 30)
    private String telefono;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "contrasena", nullable = false, length = 255)
    private String contrasena;

    // En la BD es varchar(512) según la imagen; almacenamos como String
    @Column(name = "foto_perfil", length = 512)
    private String fotoPerfil;

    // Ajustar columnDefinition para reflejar default CURRENT_TIMESTAMP en la BD
    @Column(name = "creado_en", nullable = false, updatable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime actualizadoEn;

    // Inicializar roles con el valor por defecto que se ve en la BD
    @Column(name = "roles", nullable = false, length = 50, columnDefinition = "varchar(50) default 'cliente'")
    private String roles = "cliente";

    @PrePersist
    protected void onCreate() {
        // Si la BD ya pone CURRENT_TIMESTAMP, no es estrictamente necesario, pero lo dejamos para consistencia
        if (creadoEn == null) creadoEn = LocalDateTime.now();
        if (actualizadoEn == null) actualizadoEn = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        actualizadoEn = LocalDateTime.now();
    }
}

