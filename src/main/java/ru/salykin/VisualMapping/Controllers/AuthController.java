package ru.salykin.VisualMapping.Controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.salykin.VisualMapping.DTO.Token;
import ru.salykin.VisualMapping.DTO.UserDTO;
import ru.salykin.VisualMapping.Services.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    ObjectMapper mapper = new ObjectMapper();

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<Token> auth(@RequestBody UserDTO body) throws JsonProcessingException {
        System.out.println(body.getUsername());
        try {
            Token token = authService.authenticate(body);
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Token("Authentication failed"));
        }
    }
}
