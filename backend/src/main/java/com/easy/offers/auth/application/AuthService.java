package com.easy.offers.auth.application;

import com.easy.offers.shared.exception.InvalidCredentialsException;
import com.easy.offers.users.infrastructure.UserJpaEntity;
import com.easy.offers.users.infrastructure.UserJpaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * AuthService — Caso de uso de autenticación.
 *
 * Responsabilidad única: verificar credenciales y emitir un JWT.
 *
 * ¿Por qué AuthService está en la capa Application y no en Infrastructure?
 * La lógica de autenticación es un caso de uso de negocio:
 * "dado un username y password, verificar si son válidos y emitir un token".
 * Esta lógica no depende de cómo se almacenan los datos (JPA) ni de cómo
 * se firma el token (jjwt). Depende de abstracciones (interfaces).
 *
 * Dependencias de AuthService:
 * - UserJpaRepository: para buscar el usuario por username.
 *   NOTA: Aquí hay una pequeña concesión arquitectónica — AuthService
 *   depende directamente del repositorio JPA en lugar de un puerto de dominio.
 *   Para el MVP esto es aceptable. En un sistema más grande, crearíamos
 *   una interfaz UserRepository en el dominio.
 * - PasswordEncoder: para verificar el hash BCrypt (inyectado desde SecurityConfig).
 * - TokenProvider: para generar el JWT (interfaz, no la implementación concreta).
 *
 * @Service: Spring gestiona esta clase como un bean de servicio.
 * Los beans @Service son singleton por defecto.
 *
 * Requerimiento: 1.1, 1.2, 1.5, 2.3
 */
@Service
public class AuthService {

    private final UserJpaRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    /**
     * Vigencia del token en horas, leída de application.yml.
     * Por defecto 8 horas (Requerimiento 1.1).
     */
    @Value("${jwt.expiration-hours:8}")
    private int expirationHours;

    public AuthService(
            UserJpaRepository userRepository,
            PasswordEncoder passwordEncoder,
            TokenProvider tokenProvider
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    /**
     * Autentica un usuario y retorna un token JWT si las credenciales son válidas.
     *
     * Flujo:
     * 1. Buscar el usuario por username
     * 2. Verificar que el usuario existe y está activo
     * 3. Verificar que la contraseña coincide con el hash BCrypt
     * 4. Generar y retornar el JWT
     *
     * Decisión de seguridad: mensaje de error genérico
     * En todos los casos de fallo (usuario no existe, contraseña incorrecta,
     * usuario inactivo) lanzamos la MISMA excepción con el MISMO mensaje.
     * Esto evita que un atacante pueda determinar si un username existe
     * en el sistema (enumeración de usuarios).
     *
     * @param username El nombre de usuario
     * @param password La contraseña en texto plano (se verifica contra el hash)
     * @return TokenResponse con el JWT y la fecha de expiración
     * @throws InvalidCredentialsException si las credenciales son inválidas
     */
    public TokenResponse authenticate(String username, String password) {
        // Paso 1: Buscar el usuario por username
        // findByUsername retorna Optional.empty() si no existe
        UserJpaEntity user = userRepository.findByUsername(username)
                .orElseThrow(InvalidCredentialsException::new);

        // Paso 2: Verificar que el usuario está activo (Requerimiento 2.3)
        // Un usuario desactivado no puede autenticarse, aunque tenga credenciales correctas
        if (!user.isActive()) {
            throw new InvalidCredentialsException();
        }

        // Paso 3: Verificar la contraseña con BCrypt
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        // Paso 4: Generar el JWT
        // Duration.ofHours(expirationHours): 8 horas por defecto (Req 1.1)
        Duration validity = Duration.ofHours(expirationHours);
        String token = tokenProvider.generateToken(
                user.getId(),
                user.getRole().name(), // "ADMIN" o "EMPLOYEE"
                validity
        );

        // Calcular la fecha de expiración para informar al cliente
        Instant expiresAt = Instant.now().plus(validity);

        return new TokenResponse(token, expiresAt);
    }

    /**
     * TokenResponse — DTO de respuesta del login.
     *
     * Record interno de AuthService: solo se usa en este contexto.
     * El controlador lo convierte a la respuesta JSON final.
     *
     * token: el JWT que el cliente debe incluir en cada request posterior
     *        en el header: Authorization: Bearer {token}
     * expiresAt: cuándo expira el token (para que el cliente sepa cuándo renovarlo)
     */
    public record TokenResponse(String token, Instant expiresAt) {}
}
