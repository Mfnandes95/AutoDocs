package com.example.demo.application;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/")
    public String index() {
        // Redireciona para o arquivo index.html na pasta static
        return "forward:/index.html";
    }
}