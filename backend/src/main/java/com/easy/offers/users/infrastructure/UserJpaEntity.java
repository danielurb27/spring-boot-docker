package com.easy.offers.users.infrastructure;

import com.easy.offers.users.domain.UserRole;
import jakarta.persistence.*;
import java.time.Instant;

/**
 * UserJpaEntity — Entidad JPA para la tabla `users`.
 *
 * ¿Por qué existe esta clase si ya tenemos User.java en el dominio?
 * Esta es la separación central de Clean Architecture:
 *
 * User.java (dominio):
 * - Es un record inmutable, Java puro, sin anotaciones de frameworks.
 * - Representa el concepto de negocio "usuario".
 * - No sabe nada de bases de datos.
 *
 * UserJpaEntity.java (infraestructura):
 * - Es una clase mutable con anotaciones JPA (@Entity, @Column, etc.).
 * - Representa cómo se almacena un usuario en PostgreSQL.
 * - No contiene lógica de negocio.
 *
 * El UserMapper convierte entre ambas representaciones.
 *
 * ¿Por qué JPA necesita una clase mutable con constructor vacío?
 * Hibernate (la implementación de JPA) usa reflexión para:
 * 1. Crear instancias con el constructor vacío (no-arg constructor).
 * 2. Asignar valores a los campos con setters o acceso directo.
 * Los records de Java no tienen constructor vacío ni setters, por eso
 * no podemos usar records directamente como entidades JPA.
 *
 * Anotaciones JPA explicadas:
 * @Entity: marca esta clase como una entidad JPA (mapeada a una tabla).
 * @Table: especifica el nombre de la tabla en la BD.
 * @Id: marca el campo como clave primaria.
 * @GeneratedValue: la BD genera el valor automáticamente (IDENTITY = BIGSERIAL en PostgreSQL).
 * @Column: configura el mapeo de columna (nombre, nullable, longitud, etc.).
 * @Enumerated: cómo almacenar un enum. STRING = como texto ('ADMIN', 'EMPLOYEE').
 */
@Entity
@Table(name = "users")
public class UserJpaEntity {

    /**
     * @Id + @GeneratedValue(IDENTITY): la BD asigna el ID automáticamente.
     * IDENTITY usa la secuencia BIGSERIAL de PostgreSQL.
     * Alternativa: SEQUENCE (más eficiente para inserciones masivas, pero más complejo).
     * Para el MVP, IDENTITY es suficiente.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * @Column(nullable = false): mapea a la constraint NOT NULL del schema.
     * Si intentamos persistir un UserJpaEntity con fullName=null,
     * Hibernate lanzará una excepción antes de llegar a la BD.
     */
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, length = 150, unique = true)
    private String username;

    /**
     * BCrypt siempre genera hashes de exactamente 60 caracteres.
     * length = 60 es el tamaño exacto necesario.
     */
    @Column(name = "password_hash", nullable = false, length = 60)
    private String passwordHash;

    /**
     * @Enumerated(EnumType.STRING): almacena el nombre del enum como texto.
     * Alternativa: EnumType.ORDINAL (almacena el índice numérico: 0, 1).
     * Siempre preferir STRING: si reordenamos el enum, ORDINAL rompería los datos.
     * Con STRING, el valor en BD es 'ADMIN' o 'EMPLOYEE', legible directamente.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    /**
     * updatable = false: este campo se asigna al crear y nunca se actualiza.
     * Hibernate ignorará este campo en los UPDATE statements.
     * El valor lo asigna la BD con DEFAULT NOW() o la aplicación.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    // =========================================================================
    // Constructor vacío requerido por JPA/Hibernate.
    // Hibernate lo usa internamente para crear instancias al leer de la BD.
    //
    // ¿Por qué public y no protected?
    // protected solo permite acceso desde el mismo paquete o subclases.
    // UserService está en users.application (paquete diferente a users.infrastructure),
    // por lo que necesita public para poder instanciar UserJpaEntity con "new UserJpaEntity()".
    // =========================================================================
    public UserJpaEntity() {}

    // =========================================================================
    // Getters y Setters
    // JPA necesita acceso a los campos para leer y escribir valores.
    // Usamos el patrón estándar de JavaBeans (get/set).
    // =========================================================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
