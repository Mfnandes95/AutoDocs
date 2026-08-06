package com.example.demo.infrastructure.security;

import com.example.demo.domain.model.UsuarioEntity;
import com.example.demo.domain.model.UsuarioRole;
import com.example.demo.infrastructure.repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println(">>> Tentando autenticar o e-mail (Spring Security): " + username);
        UsuarioEntity usuario = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("E-mail não encontrado: " + username));

        // Obtém o nome da constante do Enum ("GESTOR", "TECNICO"...) ou assume "COLABORADOR" se nulo
        String roleName = (usuario.getRole() != null) 
                ? usuario.getRole().name() 
                : UsuarioRole.COLABORADOR.name();

        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getSenha())
                .roles(roleName) // O Spring adiciona automaticamente o prefixo "ROLE_"
                .build();
    }

    /**
     * Autenticação manual para controllers customizados.
     */
    public UsuarioEntity autenticar(String email, String senhaDigitada) {
        System.out.println(">>> Verificando credenciais no AuthService para: " + email);
        
        UsuarioEntity usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("E-mail não encontrado"));

        if (!passwordEncoder.matches(senhaDigitada, usuario.getSenha())) {
            throw new RuntimeException("Senha incorreta");
        }

        return usuario;
    }
}