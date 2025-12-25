package ru.salykin.VisualMapping.Services;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.salykin.VisualMapping.DTO.Token;
import ru.salykin.VisualMapping.DTO.UserDTO;
import ru.salykin.VisualMapping.Repositories.UserRepository;
import ru.salykin.VisualMapping.Utils.JwtTokenUtil;

import java.util.UUID;

@Service
public class AuthService {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtTokenUtil jwtTokenUtil;

    public AuthService(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder, UserRepository userRepository, JwtTokenUtil jwtTokenUtil) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    public Token authenticate(UserDTO userDTO) {
        System.out.println(userDTO.getUsername());
        UserDetails userDetails = userDetailsService.loadUserByUsername(userDTO.getUsername());

        System.out.println(userDetails.getUsername());

        if (!(userDTO.getPassword().equals(userDetails.getPassword()))) {
            throw new BadCredentialsException("Invalid password");
        }
        String token = jwtTokenUtil.generateToken(userDetails);

        return new Token(token);
    }


    private String generateToken() {
        return UUID.randomUUID().toString();
    }
}
