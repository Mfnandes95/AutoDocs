package com.example.demo.infrastructure.security;

import com.example.demo.domain.model.UsuarioEntity;
import com.example.demo.domain.model.UsuarioRole;
import com.example.demo.infrastructure.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Normaliza (trim + lowercase) para bater com o e-mail salvo no cadastro
        // e não falhar login por diferença de maiúscula/minúscula ou espaço.
        String emailNormalizado = username == null ? "" : username.trim().toLowerCase();

        logger.debug("Tentativa de autenticação para e-mail normalizado: {}", emailNormalizado);

        UsuarioEntity usuario = userRepository.findByEmailIgnoreCase(emailNormalizado)
                .orElseThrow(() -> new UsernameNotFoundException("E-mail não encontrado: " + emailNormalizado));

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
}