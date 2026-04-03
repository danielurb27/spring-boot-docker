package com.easy.offers.auth.infrastructure;

import com.easy.offers.auth.application.TokenClaims;
import com.easy.offers.auth.application.TokenProvider;
import com.easy.offers.shared.exception.TokenExpiredException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JwtAuthFilter — Filtro que intercepta cada request HTTP y valida el JWT.
 *
 * ¿Qué es un filtro en Spring Security?
 * Spring Security funciona como una cadena de filtros (Filter Chain).
 * Cada request HTTP pasa por todos los filtros en orden antes de llegar
 * al controlador. Los filtros pueden:
 * - Leer/modificar la request
 * - Autenticar al usuario
 * - Rechazar la request (retornar 401/403)
 * - Pasar la request al siguiente filtro
 *
 * ¿Por qué OncePerRequestFilter?
 * Garantiza que el filtro se ejecuta exactamente UNA vez por request,
 * incluso en casos de forward/include internos de Servlet.
 * Sin esto, el filtro podría ejecutarse múltiples veces por request.
 *
 * Flujo de autenticación JWT:
 *
 * [Request] → JwtAuthFilter → [Validar JWT] → [Cargar SecurityContext] → [Controller]
 *                                   │
 *                                   └── Si inválido: continuar sin autenticar
 *                                       (Spring Security rechazará el acceso a endpoints protegidos)
 *
 * Decisión importante: este filtro NO lanza excepciones.
 * Si el token es inválido o ausente, simplemente no carga el SecurityContext.
 * Spring Security luego rechazará el acceso a endpoints protegidos con 401.
 * Esto es más limpio que lanzar excepciones en el filtro.
 *
 * Requerimiento: 1.3 — Rechazar requests sin JWT o con JWT expirado.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;

    /**
     * Spring inyecta TokenProvider (la interfaz), no JwtProvider directamente.
     * Esto respeta el Principio de Inversión de Dependencias.
     */
    public JwtAuthFilter(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    /**
     * Método principal del filtro. Se ejecuta en cada request HTTP.
     *
     * @param request     La request HTTP entrante
     * @param response    La response HTTP saliente
     * @param filterChain La cadena de filtros (para pasar al siguiente filtro)
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Paso 1: Extraer el token del header Authorization
        String token = extractToken(request);

        // Paso 2: Si hay token, validarlo y cargar el SecurityContext
        if (token != null) {
            try {
                // Validar el token y extraer los claims
                TokenClaims claims = tokenProvider.validateToken(token);

                // Paso 3: Crear el objeto de autenticación de Spring Security
                // UsernamePasswordAuthenticationToken: representa un usuario autenticado
                // - principal: el userId (lo que identifica al usuario)
                // - credentials: null (ya autenticado, no necesitamos la contraseña)
                // - authorities: los roles del usuario (prefijo ROLE_ requerido por Spring Security)
                //
                // ¿Por qué "ROLE_" + role?
                // Spring Security usa el prefijo "ROLE_" por convención.
                // @PreAuthorize("hasRole('ADMIN')") busca la authority "ROLE_ADMIN".
                // Sin el prefijo, la autorización no funcionaría.
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                claims.userId(),                                    // principal
                                null,                                               // credentials
                                List.of(new SimpleGrantedAuthority("ROLE_" + claims.role())) // authorities
                        );

                // Paso 4: Cargar el SecurityContext con la autenticación
                // SecurityContextHolder es el almacén thread-local de Spring Security.
                // Al cargarlo aquí, todos los componentes del request (controllers,
                // services) pueden acceder al usuario autenticado con:
                // SecurityContextHolder.getContext().getAuthentication()
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (TokenExpiredException e) {
                // Token inválido o expirado: limpiar el contexto y continuar sin autenticar.
                // Spring Security rechazará el acceso a endpoints protegidos con 401.
                // No lanzamos la excepción aquí — dejamos que Spring Security maneje el 401.
                SecurityContextHolder.clearContext();
            }
        }

        // Paso 5: Pasar la request al siguiente filtro en la cadena
        // Siempre llamamos a filterChain.doFilter(), incluso si no hay token.
        // Spring Security decidirá si el endpoint requiere autenticación o no.
        filterChain.doFilter(request, response);
    }

    /**
     * Extrae el token JWT del header Authorization.
     *
     * El header tiene el formato: "Authorization: Bearer eyJhbGci..."
     * Necesitamos extraer solo la parte después de "Bearer ".
     *
     * @param request La request HTTP
     * @return El token JWT sin el prefijo "Bearer ", o null si no hay header
     */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        // Verificar que el header existe y tiene el formato correcto
        // "Bearer " tiene 7 caracteres (incluyendo el espacio)
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // Extraer todo después de "Bearer "
        }

        return null; // No hay token en esta request
    }
}
