package com.LamparasShoop.Controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.LamparasShoop.Model.Usuario;
import com.LamparasShoop.Repository.ProductoRepository;
import com.LamparasShoop.Repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    @GetMapping("/")
    public String index(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            String username = auth.getName();
            try {
                Usuario usuario = usuarioRepository.findByUsername(username)
                        .orElse(null);
                model.addAttribute("usuario", usuario);
            } catch (Exception e) {
                model.addAttribute("usuario", null);
            }
        } else {
            model.addAttribute("usuario", null);
        }

        model.addAttribute("productos", productoRepository.findAll());
        return "index";
    }
}
