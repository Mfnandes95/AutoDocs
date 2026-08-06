package com.example.demo.application;

import com.example.demo.domain.model.UsuarioEntity;
import com.example.demo.domain.model.UsuarioRole;
import com.example.demo.infrastructure.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CadastroController {

    private static final Logger logger = LoggerFactory.getLogger(CadastroController.class);

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

        // Normaliza o e-mail (trim + lowercase) para casar com a busca
        // case-insensitive usada no login (AuthService). A senha NÃO é
        // trimada: espaços podem fazer parte legítima de uma senha, e
        // trimar aqui sem trimar no login (ou vice-versa) é justamente o
        // tipo de assimetria que quebra autenticação "silenciosamente".
        String emailNormalizado = email == null ? "" : email.trim().toLowerCase();
        String nomeNormalizado = (nome != null && !nome.isBlank()) ? nome.trim() : emailNormalizado;

        if (emailNormalizado.isBlank()) {
            return "redirect:/register.html?error=email_invalido";
        }

        // Senhas não conferem
        if (senha == null || !senha.equals(confirmSenha)) {
            return "redirect:/register.html?error=senha";
        }

        // Senha muito curta
        if (senha.length() < 6) {
            return "redirect:/register.html?error=senha_curta";
        }

        // E-mail já cadastrado (case-insensitive)
        if (userRepository.existsByEmailIgnoreCase(emailNormalizado)) {
            return "redirect:/register.html?error=exists";
        }

        UsuarioEntity novo = new UsuarioEntity();
        novo.setNome(nomeNormalizado);
        novo.setEmail(emailNormalizado);
        novo.setSenha(passwordEncoder.encode(senha)); // ← BCrypt aqui
        novo.setRole(UsuarioRole.COLABORADOR);               // ← enum, não String

        userRepository.save(novo);

        logger.info("Novo usuário registrado (id={})", novo.getId());

        return "redirect:/login.html?registered";
    }
}