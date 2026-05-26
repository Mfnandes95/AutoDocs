package com.example.demo.infrastructure.security;

import com.example.demo.domain.model.UsuarioEntity;
import com.example.demo.infrastructure.repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder; // Adicionado
import org.springframework.stereotype.Service;

@Service
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Adicionado para comparar os hashes BCrypt

    // Injetando ambos os beans via construtor
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println(">>> Tentando autenticar o e-mail (Spring Security): " + username);
        UsuarioEntity usuario = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("E-mail não encontrado: " + username));

        return User.builder()
        .username(usuario.getEmail())
        .password(usuario.getSenha())
        .roles(usuario.getRole().name())
        .build();
    }

    // O MÉTODO QUE ESTAVA FALTANDO: Chamado pelo seu AuthController manual
    public UsuarioEntity autenticar(String email, String senhaDigitada) {
        System.out.println(">>> Verificando credenciais no AuthService para: " + email);
        
        // 1. Busca o usuário usando o seu UserRepository
        UsuarioEntity usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("E-mail não encontrado"));

        // 2. Compara a senha digitada em texto puro com o hash BCrypt do banco
        if (!passwordEncoder.matches(senhaDigitada, usuario.getSenha())) {
            throw new RuntimeException("Senha incorreta");
        }

        // 3. Se a senha bater, retorna o usuário para o Controller montar o JSON
        return usuario;
    }
}