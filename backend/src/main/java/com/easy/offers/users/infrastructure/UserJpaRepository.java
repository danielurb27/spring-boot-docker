package com.easy.offers.users.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserJpaRepository — Repositorio Spring Data JPA para la tabla `users`.
 *
 * ¿Qué es Spring Data JPA?
 * Es una capa de abstracción sobre JPA que genera automáticamente las consultas
 * SQL a partir de los nombres de los métodos o de anotaciones @Query.
 *
 * Al extender JpaRepository<UserJpaEntity, Long>, obtenemos GRATIS:
 * - save(entity): INSERT o UPDATE
 * - findById(id): SELECT WHERE id = ?
 * - findAll(): SELECT * FROM users
 * - delete(entity): DELETE WHERE id = ?
 * - existsById(id): SELECT COUNT(*) WHERE id = ?
 * - count(): SELECT COUNT(*) FROM users
 * Y muchos más métodos estándar de CRUD.
 *
 * Métodos derivados (Query Methods):
 * Spring Data genera el SQL automáticamente a partir del nombre del método.
 * Convención: findBy + NombreCampo + Condición
 *
 * Ejemplos:
 * findByUsername(String username) → SELECT * FROM users WHERE username = ?
 * findByUsernameAndIsActive(String username, boolean isActive) → WHERE username = ? AND is_active = ?
 * existsByUsername(String username) → SELECT COUNT(*) > 0 WHERE username = ?
 *
 * @Repository: marca esta interfaz como un repositorio de Spring.
 * Spring Data crea automáticamente la implementación en tiempo de ejecución.
 * No necesitamos escribir ninguna clase que implemente esta interfaz.
 */
@Repository
public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {

    /**
     * Busca un usuario por su username.
     * Usado en AuthService para verificar credenciales al hacer login.
     *
     * Optional<T>: retorna Optional.empty() si no existe, en lugar de null.
     * Esto obliga al código que llama a manejar el caso "no encontrado"
     * explícitamente, evitando NullPointerExceptions.
     *
     * SQL generado: SELECT * FROM users WHERE username = ?
     */
    Optional<UserJpaEntity> findByUsername(String username);

    /**
     * Verifica si ya existe un usuario con ese username.
     * Usado en UserService para validar unicidad antes de crear un usuario.
     * Más eficiente que findByUsername porque solo retorna un boolean.
     *
     * SQL generado: SELECT COUNT(*) > 0 FROM users WHERE username = ?
     */
    boolean existsByUsername(String username);
}
