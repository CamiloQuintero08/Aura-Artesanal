package com.LamparasShoop.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.LamparasShoop.Model.Usuario;
import com.LamparasShoop.Service.UsuarioService;

@Controller
@RequestMapping("/perfil")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Muestra la página de perfil del usuario autenticado
     */
    @GetMapping
    public String mostrarPerfil(Model model) {
        try {
            Usuario usuario = usuarioService.getUsuarioAutenticado();
            model.addAttribute("usuario", usuario);
            return "perfil";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar el perfil: " + e.getMessage());
            return "redirect:/index";
        }
    }

    /**
     * Actualiza la información del perfil
     */
    @PostMapping("/actualizar")
    public String actualizarPerfil(@ModelAttribute Usuario usuarioActualizado,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario usuarioAutenticado = usuarioService.getUsuarioAutenticado();
            usuarioService.actualizarPerfil(usuarioAutenticado.getId(), usuarioActualizado);

            redirectAttributes.addFlashAttribute("success", "Perfil actualizado correctamente");
            return "redirect:/perfil";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/perfil";
        }
    }

    /**
     * Cambia la contraseña del usuario
     */
    @PostMapping("/cambiar-password")
    public String cambiarPassword(@RequestParam("passwordActual") String passwordActual,
            @RequestParam("passwordNueva") String passwordNueva,
            @RequestParam("passwordConfirmar") String passwordConfirmar,
            RedirectAttributes redirectAttributes) {
        try {
            // Validar que las contraseñas nuevas coincidan
            if (!passwordNueva.equals(passwordConfirmar)) {
                redirectAttributes.addFlashAttribute("errorPassword", "Las contraseñas nuevas no coinciden");
                return "redirect:/perfil";
            }

            // Validar longitud mínima
            if (passwordNueva.length() < 6) {
                redirectAttributes.addFlashAttribute("errorPassword", "La contraseña debe tener al menos 6 caracteres");
                return "redirect:/perfil";
            }

            Usuario usuarioAutenticado = usuarioService.getUsuarioAutenticado();
            usuarioService.cambiarPassword(usuarioAutenticado.getId(), passwordActual, passwordNueva);

            redirectAttributes.addFlashAttribute("successPassword", "Contraseña cambiada correctamente");
            return "redirect:/perfil";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorPassword", e.getMessage());
            return "redirect:/perfil";
        }
    }
}
