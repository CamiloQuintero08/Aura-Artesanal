package com.LamparasShoop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                // 🔒 Configuración de rutas públicas y protegidas
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/login", "/registro", "/img/**").permitAll()
                                                .requestMatchers("/productos/nuevo", "/productos/guardar",
                                                                "/productos/editar/**",
                                                                "/productos/eliminar/**")
                                                .hasRole("ADMIN")
                                                .requestMatchers("/perfil/**").authenticated()
                                                .requestMatchers("/api/carrito/**").authenticated()
                                                .anyRequest().authenticated())

                                // 🔑 Configuración del formulario de login
                                .formLogin(form -> form
                                                .loginPage("/login") // Vista personalizada de login
                                                .loginProcessingUrl("/login") // Acción que procesa el formulario
                                                .defaultSuccessUrl("/index", true) // Página después del login exitoso
                                                .failureUrl("/login?error") // En caso de error de autenticación
                                                .permitAll())

                                // 🚪 Configuración del logout
                                .logout(logout -> logout
                                                .logoutUrl("/logout") // URL para cerrar sesión
                                                .logoutSuccessUrl("/login?logout") // Redirige al login después del
                                                                                   // logout
                                                .invalidateHttpSession(true) // Elimina la sesión
                                                .clearAuthentication(true) // Limpia autenticación
                                                .deleteCookies("JSESSIONID") // Elimina cookie de sesión
                                                .permitAll())

                                // ❌ Deshabilitamos CSRF solo para desarrollo (puedes activarlo más adelante)
                                .csrf(csrf -> csrf.disable());

                return http.build();
        }

        // 🧠 Bean para la autenticación
        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
                return authConfig.getAuthenticationManager();
        }

        // 🔐 Bean para encriptar contraseñas
        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}
