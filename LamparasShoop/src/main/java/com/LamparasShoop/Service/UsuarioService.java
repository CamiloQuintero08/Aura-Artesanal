package com.LamparasShoop.Service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.LamparasShoop.Model.Usuario;
import com.LamparasShoop.Repository.UsuarioRepository;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Obtiene el usuario autenticado actualmente
     */
    public Usuario getUsuarioAutenticado() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("[DEBUG] Authentication object: " + auth);
            System.out.println("[DEBUG] Is authenticated: " + (auth != null ? auth.isAuthenticated() : "null"));
            System.out.println("[DEBUG] Principal: " + (auth != null ? auth.getPrincipal() : "null"));

            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                throw new RuntimeException("Usuario no autenticado");
            }

            String username = auth.getName();
            System.out.println("[DEBUG] Username from auth: " + username);

            return usuarioRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
        } catch (Exception e) {
            System.err.println("[ERROR] Error en getUsuarioAutenticado: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Obtiene un usuario por ID
     */
    public Optional<Usuario> getUsuarioById(Long id) {
        return usuarioRepository.findById(id);
    }

    /**
     * Actualiza la información del perfil del usuario
     */
    public Usuario actualizarPerfil(Long id, Usuario datosActualizados) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Validar que el usuario solo pueda editar su propio perfil
        Usuario usuarioAutenticado = getUsuarioAutenticado();
        if (!usuario.getId().equals(usuarioAutenticado.getId())) {
            throw new RuntimeException("No tienes permiso para editar este perfil");
        }

        // Validar unicidad de username si cambió
        if (!usuario.getUsername().equals(datosActualizados.getUsername())) {
            if (usuarioRepository.findByUsername(datosActualizados.getUsername()).isPresent()) {
                throw new RuntimeException("El nombre de usuario ya está en uso");
            }
            usuario.setUsername(datosActualizados.getUsername());
        }

        // Validar unicidad de email si cambió
        if (!usuario.getEmail().equals(datosActualizados.getEmail())) {
            Optional<Usuario> usuarioConEmail = usuarioRepository.findAll().stream()
                    .filter(u -> u.getEmail().equals(datosActualizados.getEmail()) && !u.getId().equals(id))
                    .findFirst();
            if (usuarioConEmail.isPresent()) {
                throw new RuntimeException("El email ya está en uso");
            }
            usuario.setEmail(datosActualizados.getEmail());
        }

        // Validar unicidad de teléfono si cambió
        if (!usuario.getPhone().equals(datosActualizados.getPhone())) {
            Optional<Usuario> usuarioConPhone = usuarioRepository.findAll().stream()
                    .filter(u -> u.getPhone().equals(datosActualizados.getPhone()) && !u.getId().equals(id))
                    .findFirst();
            if (usuarioConPhone.isPresent()) {
                throw new RuntimeException("El teléfono ya está en uso");
            }
            usuario.setPhone(datosActualizados.getPhone());
        }

        // Actualizar campos
        usuario.setName(datosActualizados.getName());
        usuario.setLastName(datosActualizados.getLastName());
        usuario.setDateOfBirth(datosActualizados.getDateOfBirth());

        return usuarioRepository.save(usuario);
    }

    /**
     * Cambia la contraseña del usuario
     */
    public void cambiarPassword(Long id, String passwordActual, String passwordNueva) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Validar que el usuario solo pueda cambiar su propia contraseña
        Usuario usuarioAutenticado = getUsuarioAutenticado();
        if (!usuario.getId().equals(usuarioAutenticado.getId())) {
            throw new RuntimeException("No tienes permiso para cambiar esta contraseña");
        }

        // Verificar que la contraseña actual sea correcta
        if (!passwordEncoder.matches(passwordActual, usuario.getPassword())) {
            throw new RuntimeException("La contraseña actual es incorrecta");
        }

        // Encriptar y guardar la nueva contraseña
        usuario.setPassword(passwordEncoder.encode(passwordNueva));
        usuarioRepository.save(usuario);
    }
}
