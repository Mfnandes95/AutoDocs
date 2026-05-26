package com.example.demo.application;

import com.example.demo.domain.model.UsuarioEntity;
import com.example.demo.domain.model.UsuarioRole;
import com.example.demo.infrastructure.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CadastroController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CadastroController(UserRepository userRepository,
                               PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public String cadastrar(
            @RequestParam(required = false) String nome,
            @RequestParam String email,
            @RequestParam String senha,
            @RequestParam String confirmSenha) {

        // Senhas não conferem
        if (!senha.equals(confirmSenha)) {
            return "redirect:/register.html?error=senha";
        }

        // Senha muito curta
        if (senha.length() < 6) {
            return "redirect:/register.html?error=senha_curta";
        }

        // E-mail já cadastrado
        if (userRepository.findByEmail(email).isPresent()) {
            return "redirect:/register.html?error=exists";
        }

        UsuarioEntity novo = new UsuarioEntity();
        novo.setNome(nome != null && !nome.isBlank() ? nome : email);
        novo.setEmail(email);
        novo.setSenha(passwordEncoder.encode(senha)); // ← BCrypt aqui
        novo.setRole(UsuarioRole.COLABORADOR);               // ← enum, não String

        userRepository.save(novo);

        System.out.println("[CADASTRO] Usuário registrado: " + email);

        return "redirect:/login.html?registered";
    }
}