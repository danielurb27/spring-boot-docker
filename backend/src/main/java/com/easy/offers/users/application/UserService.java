package com.easy.offers.users.application;

import com.easy.offers.shared.exception.DuplicateUsernameException;
import com.easy.offers.shared.exception.InvalidRoleException;
import com.easy.offers.shared.exception.ResourceNotFoundException;
import com.easy.offers.users.domain.User;
import com.easy.offers.users.domain.UserRole;
import com.easy.offers.users.infrastructure.UserJpaEntity;
import com.easy.offers.users.infrastructure.UserJpaRepository;
import com.easy.offers.users.infrastructure.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * UserService — Casos de uso para la gestión de usuarios.
 *
 * Responsabilidades:
 * 1. Crear nuevos usuarios (solo ADMIN puede invocar esto, controlado en el Controller)
 * 2. Desactivar usuarios existentes (soft delete)
 *
 * @Service: Spring gestiona esta clase como un bean de servicio singleton.
 *
 * @Transactional: garantiza que cada operación de escritura sea atómica.
 * Si algo falla en el medio de createUser (ej: error al guardar), la transacción
 * se revierte automáticamente y la BD queda en su estado anterior.
 * Sin @Transactional, podríamos tener datos parcialmente guardados.
 *
 * Requerimiento: 2.1, 2.2, 2.3, 2.5
 */
@Service
@Transactional
public class UserService {

    private final UserJpaRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserJpaRepository userRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Crea un nuevo usuario en el sistema.
     *
     * Flujo:
     * 1. Validar que el rol sea válido (ADMIN o EMPLOYEE)
     * 2. Verificar que el username no esté en uso
     * 3. Hashear la contraseña con BCrypt
     * 4. Persistir el usuario con is_active = true
     * 5. Retornar el usuario creado (sin el hash de contraseña)
     *
     * @param fullName Nombre completo del usuario
     * @param username Nombre de usuario único
     * @param rawPassword Contraseña en texto plano (se hashea antes de guardar)
     * @param roleStr Rol como String ("ADMIN" o "EMPLOYEE")
     * @return El usuario creado como entidad de dominio
     * @throws InvalidRoleException si el rol no es válido
     * @throws DuplicateUsernameException si el username ya existe
     */
    public User createUser(String fullName, String username, String rawPassword, String roleStr) {

        // Paso 1: Validar el rol
        // Intentamos convertir el String al enum UserRole.
        // Si el valor no existe en el enum, valueOf() lanza IllegalArgumentException.
        // La capturamos y lanzamos nuestra excepción de dominio.
        UserRole role;
        try {
            role = UserRole.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidRoleException(roleStr);
        }

        // Paso 2: Verificar unicidad del username (Requerimiento 2.2)
        // existsByUsername es más eficiente que findByUsername porque solo
        // retorna un boolean sin cargar el objeto completo.
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateUsernameException(username);
        }

        // Paso 3: Hashear la contraseña con BCrypt factor 10 (Requerimiento 1.5)
        // passwordEncoder.encode() genera un hash BCrypt con salt aleatorio.
        // El resultado siempre tiene 60 caracteres: $2a$10$<salt><hash>
        String passwordHash = passwordEncoder.encode(rawPassword);

        // Paso 4: Construir la entidad JPA y persistir
        // Usamos la entidad JPA directamente aquí para simplificar.
        // En un sistema más grande, crearíamos primero la entidad de dominio
        // y luego la convertiríamos con el mapper.
        UserJpaEntity entity = new UserJpaEntity();
        entity.setFullName(fullName);
        entity.setUsername(username);
        entity.setPasswordHash(passwordHash);
        entity.setRole(role);
        entity.setActive(true);                // Requerimiento 2.1: is_active = true al crear
        entity.setCreatedAt(Instant.now());    // Timestamp de creación

        UserJpaEntity saved = userRepository.save(entity);

        // Paso 5: Convertir a entidad de dominio y retornar
        // El mapper convierte UserJpaEntity → User (dominio)
        return userMapper.toDomain(saved);
    }

    /**
     * Desactiva un usuario existente (soft delete).
     *
     * En lugar de eliminar el registro de la BD (lo que rompería las FKs
     * de auditoría y ofertas), marcamos is_active = false.
     * El usuario desactivado no puede autenticarse (AuthService lo verifica).
     *
     * @param userId ID del usuario a desactivar
     * @return El usuario actualizado como entidad de dominio
     * @throws ResourceNotFoundException si el usuario no existe
     *
     * Requerimiento: 2.3 — Usuario desactivado no puede autenticarse.
     */
    public User deactivateUser(Long userId) {
        // Buscar el usuario o lanzar 404
        UserJpaEntity entity = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", userId));

        // Marcar como inactivo
        entity.setActive(false);
        entity.setUpdatedAt(Instant.now());

        // save() con un ID existente hace UPDATE en lugar de INSERT
        UserJpaEntity saved = userRepository.save(entity);

        return userMapper.toDomain(saved);
    }

    /**
     * Obtiene todos los usuarios del sistema.
     * Usado por el endpoint GET /api/users para listar usuarios en el frontend.
     */
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDomain)
                .toList();
    }

    /**
     * Obtiene un usuario por su ID.
     * Usado internamente y por el controlador para retornar datos del usuario.
     *
     * @Transactional(readOnly = true): optimización para operaciones de solo lectura.
     * Hibernate no rastrea cambios en las entidades (no dirty checking),
     * lo que mejora el rendimiento.
     */
    @Transactional(readOnly = true)
    public User findById(Long userId) {
        return userRepository.findById(userId)
                .map(userMapper::toDomain)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", userId));
    }
}
