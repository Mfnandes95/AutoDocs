package com.example.demo.application; // Ajuste para o pacote do seu projeto

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class FaviconController {

    @GetMapping("favicon.ico")
    @ResponseBody
    public void returnNoFavicon() {
        // Apenas retorna vazio para calar o navegador e evitar o erro 500/404 no login
    }
}