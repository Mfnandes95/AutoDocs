package com.example.demo.application;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    // Página Inicial / Index — única SPA real do projeto.
    @GetMapping({"/", "/index", "/index.html"})
    public String indexPage() {
        return "index"; // Procura por templates/index.html
    }

    // As rotas abaixo NÃO têm templates próprios (login.html, register.html,
    // dashboard.html e inventario.html nunca existiram em templates/) —
    // acessá-las diretamente pela URL gerava erro 500 do Thymeleaf
    // (TemplateInputException). A navegação real acontece toda dentro da
    // index.html via JS (app.js troca as seções #view-auth / #view-app).
    // Por isso essas URLs agora redirecionam para a SPA em vez de tentar
    // renderizar uma view inexistente.
    @GetMapping({"/login", "/login.html", "/register", "/register.html",
                 "/dashboard", "/dashboard.html", "/inventario", "/inventario.html"})
    public String redirectParaSpa() {
        return "redirect:/index.html";
    }
}