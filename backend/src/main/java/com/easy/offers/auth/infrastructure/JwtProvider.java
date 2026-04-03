package com.easy.offers.auth.infrastructure;

import com.easy.offers.auth.application.TokenClaims;
import com.easy.offers.auth.application.TokenProvider;
import com.easy.offers.shared.exception.TokenExpiredException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * JwtProvider — Implementación de TokenProvider usando la librería jjwt.
 *
 * Esta clase vive en la capa Infrastructure porque depende de una librería
 * externa (io.jsonwebtoken). La capa Application solo conoce la interfaz
 * TokenProvider, no esta implementación concreta.
 *
 * ¿Cómo funciona un JWT?
 * Un JWT tiene tres partes separadas por puntos: header.payload.signature
 *
 * 1. Header (Base64URL): algoritmo de firma y tipo de token
 *    {"alg": "HS256", "typ": "JWT"}
 *
 * 2. Payload (Base64URL): los claims (datos del usuario)
 *    {"sub": "42", "role": "ADMIN", "iat": 1720000000, "exp": 1720028800}
 *
 * 3. Signature: HMAC-SHA256(base64(header) + "." + base64(payload), secretKey)
 *    La firma garantiza que el token no fue alterado.
 *
 * Ejemplo de token real:
 * eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI0MiIsInJvbGUiOiJBRE1JTiJ9.abc123...
 *
 * ¿Por qué HS256 (HMAC-SHA256)?
 * - Es simétrico: la misma clave firma y verifica. Simple para un monolito.
 * - Alternativa: RS256 (asimétrico, clave privada firma, clave pública verifica).
 *   RS256 es mejor para microservicios donde múltiples servicios verifican tokens
 *   sin necesitar la clave privada. Para nuestro monolito, HS256 es suficiente.
 *
 * @Value: Spring inyecta el valor de la propiedad jwt.secret del application.yml.
 * La clave viene de la variable de entorno JWT_SECRET (Requerimiento 11.3).
 *
 * @Component: Spring gestiona esta clase como un bean singleton.
 * Se inyecta en AuthService a través de la interfaz TokenProvider.
 */
@Component
public class JwtProvider implements TokenProvider {

    /**
     * Clave secreta para firmar los tokens JWT.
     * Se inyecta desde application.yml → variable de entorno JWT_SECRET.
     *
     * Requisito de seguridad: debe tener al menos 256 bits (32 bytes) para HS256.
     * Si la clave es muy corta, jjwt lanzará una excepción al arrancar.
     */
    private final SecretKey secretKey;

    /**
     * Constructor: convierte el String de la clave a un SecretKey de Java.
     *
     * @Value("${jwt.secret}"): Spring inyecta el valor de jwt.secret del application.yml.
     * Keys.hmacShaKeyFor(): convierte los bytes de la clave a un objeto SecretKey
     * compatible con el algoritmo HMAC-SHA256.
     */
    public JwtProvider(@Value("${jwt.secret}") String secret) {
        // Convertir el String a bytes UTF-8 y luego a SecretKey
        // StandardCharsets.UTF_8: siempre usar UTF-8 para consistencia entre plataformas
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Genera un token JWT firmado con HS256.
     *
     * Flujo:
     * 1. Calcular la fecha de expiración (now + validity)
     * 2. Construir el JWT con los claims del usuario
     * 3. Firmar con la clave secreta usando HS256
     * 4. Serializar a String (formato: header.payload.signature)
     */
    @Override
    public String generateToken(Long userId, String role, Duration validity) {
        Instant now = Instant.now();
        Instant expiration = now.plus(validity);

        return Jwts.builder()
                // sub (subject): identificador del usuario como String
                // Los claims de JWT son siempre strings, convertimos Long → String
                .subject(String.valueOf(userId))

                // Claim personalizado: rol del usuario
                // Será extraído en validateToken para cargar el SecurityContext
                .claim("role", role)

                // iat (issued at): cuándo se emitió el token
                .issuedAt(Date.from(now))

                // exp (expiration): cuándo expira el token
                // Date.from(Instant): conversión necesaria porque jjwt usa java.util.Date
                .expiration(Date.from(expiration))

                // Firmar con HS256 y nuestra clave secreta
                .signWith(secretKey)

                // Serializar a String compacto: header.payload.signature
                .compact();
    }

    /**
     * Valida un token JWT y extrae sus claims.
     *
     * Flujo:
     * 1. Parsear el token (verifica formato)
     * 2. Verificar la firma (con nuestra clave secreta)
     * 3. Verificar que no esté expirado
     * 4. Extraer los claims y construir TokenClaims
     *
     * Si cualquier verificación falla, jjwt lanza una excepción que
     * capturamos y convertimos a nuestra excepción de dominio TokenExpiredException.
     */
    @Override
    public TokenClaims validateToken(String token) {
        try {
            // Jwts.parser(): construye un parser configurado con nuestra clave
            // verifyWith(secretKey): verifica que la firma sea válida
            // parseSignedClaims(token): parsea y valida el token completo
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Extraer el userId del claim "sub" (siempre es String en JWT)
            Long userId = Long.parseLong(claims.getSubject());

            // Extraer el rol del claim personalizado "role"
            String role = claims.get("role", String.class);

            // Extraer la fecha de expiración y convertir a Instant
            Instant expiresAt = claims.getExpiration().toInstant();

            return new TokenClaims(userId, role, expiresAt);

        } catch (ExpiredJwtException e) {
            // El token es válido pero ya expiró
            throw new TokenExpiredException("El token JWT ha expirado. Por favor inicie sesión nuevamente.", e);

        } catch (JwtException | IllegalArgumentException e) {
            // Token malformado, firma inválida, o token vacío/null
            throw new TokenExpiredException("Token JWT inválido o malformado.", e);
        }
    }
}
