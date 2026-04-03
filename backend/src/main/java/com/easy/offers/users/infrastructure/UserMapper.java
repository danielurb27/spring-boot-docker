package com.easy.offers.users.infrastructure;

import com.easy.offers.users.domain.User;
import org.springframework.stereotype.Component;

/**
 * UserMapper — Convierte entre User (dominio) y UserJpaEntity (infraestructura).
 *
 * ¿Qué es un mapper?
 * Es una clase cuya única responsabilidad es traducir entre dos representaciones
 * del mismo dato. En Clean Architecture, el mapper vive en la capa Infrastructure
 * porque es el "puente" entre el dominio puro y el framework de persistencia.
 *
 * ¿Por qué no usar una librería como MapStruct?
 * MapStruct genera código de mapeo automáticamente en tiempo de compilación.
 * Para el MVP, el mapeo manual es más explícito y educativo.
 * En un proyecto real con muchas entidades, MapStruct ahorraría mucho código.
 *
 * Flujo de datos:
 *
 * [API Request] → DTO → [Service] → User (dominio) → UserMapper → UserJpaEntity → [BD]
 * [BD] → UserJpaEntity → UserMapper → User (dominio) → [Service] → DTO → [API Response]
 *
 * @Component: Spring gestiona esta clase como un bean singleton.
 * Se inyecta en los repositorios o servicios que necesiten hacer conversiones.
 */
@Component
public class UserMapper {

    /**
     * Convierte una entidad JPA a una entidad de dominio.
     * Se usa al LEER de la base de datos.
     *
     * @param entity La entidad JPA leída de la BD (puede ser null si no se encontró)
     * @return La entidad de dominio, o null si la entidad JPA es null
     */
    public User toDomain(UserJpaEntity entity) {
        if (entity == null) return null;

        return new User(
                entity.getId(),
                entity.getFullName(),
                entity.getUsername(),
                entity.getPasswordHash(),
                entity.getRole(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    /**
     * Convierte una entidad de dominio a una entidad JPA.
     * Se usa al ESCRIBIR en la base de datos (INSERT o UPDATE).
     *
     * Nota: si user.id() es null, Hibernate interpretará que es un INSERT nuevo.
     * Si user.id() tiene valor, Hibernate hará un UPDATE (o merge).
     *
     * @param user La entidad de dominio a persistir
     * @return La entidad JPA lista para ser guardada con el repositorio
     */
    public UserJpaEntity toJpa(User user) {
        if (user == null) return null;

        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(user.id());
        entity.setFullName(user.fullName());
        entity.setUsername(user.username());
        entity.setPasswordHash(user.passwordHash());
        entity.setRole(user.role());
        entity.setActive(user.isActive());
        entity.setCreatedAt(user.createdAt());
        entity.setUpdatedAt(user.updatedAt());
        return entity;
    }
}
