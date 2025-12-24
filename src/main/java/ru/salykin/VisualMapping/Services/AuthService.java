package ru.salykin.VisualMapping.Services;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.salykin.VisualMapping.DTO.Token;
import ru.salykin.VisualMapping.DTO.UserDTO;
import ru.salykin.VisualMapping.Repositories.UserRepository;

import java.util.UUID;

@Service
public class AuthService {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public AuthService(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public Token authenticate(UserDTO userDTO) {
        System.out.println(userDTO.getUsername());
        UserDetails userDetails = userDetailsService.loadUserByUsername(userDTO.getUsername());

        System.out.println(userDetails.getUsername());

        if (!(userDTO.getPassword().equals(userDetails.getPassword()))) {
            throw new BadCredentialsException("Invalid password");
        }
        String token = generateToken();

        return new Token(token);
    }


    private String generateToken() {
        return UUID.randomUUID().toString();
    }
}
