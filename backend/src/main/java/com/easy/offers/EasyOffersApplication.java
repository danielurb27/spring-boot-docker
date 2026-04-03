package com.easy.offers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * EasyOffersApplication — Punto de entrada de la aplicación Spring Boot.
 *
 * @SpringBootApplication con exclude de UserDetailsServiceAutoConfiguration:
 * Spring Security, al no encontrar un UserDetailsService bean, auto-configura
 * uno propio que genera una contraseña aleatoria en los logs y usa su propio
 * mecanismo de autenticación. Esto interfiere con nuestro AuthService + JWT.
 * Al excluirlo, Spring Security usa únicamente nuestra configuración (SecurityConfig
 * + JwtAuthFilter) sin agregar ningún mecanismo de autenticación adicional.
 *
 * @EnableScheduling habilita el soporte para tareas programadas (@Scheduled).
 */
@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
@EnableScheduling
public class EasyOffersApplication {

    public static void main(String[] args) {
        SpringApplication.run(EasyOffersApplication.class, args);
    }
}
