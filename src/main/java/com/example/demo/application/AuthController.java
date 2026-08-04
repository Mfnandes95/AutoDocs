package com.example.demo.application;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    // Página Inicial / Index
    @GetMapping({"/", "/index", "/index.html"})
    public String indexPage() {
        return "index"; // Procura por templates/index.html
    }

    // Autenticação
    @GetMapping({"/login", "/login.html"})
    public String loginPage() {
        return "login"; // Procura por templates/login.html
    }

    @GetMapping({"/register", "/register.html"})
    public String registerPage() {
        return "register"; // Procura por templates/register.html
    }

    // Dashboard
    @GetMapping({"/dashboard", "/dashboard.html"})
    public String dashboardPage() {
        return "dashboard"; // Procura por templates/dashboard.html
    }

    // Inventário
    @GetMapping({"/inventario", "/inventario.html"})
    public String inventarioPage() {
        return "inventario"; // Procura por templates/inventario.html
    }
}