package ru.salykin.VisualMapping.Controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ru.salykin.VisualMapping.DTO.SchemaDTO;
import ru.salykin.VisualMapping.Services.DataTransformer;
import ru.salykin.VisualMapping.Services.JsonParserService;

@RestController
@RequestMapping("/apimapping")
@CrossOrigin(origins = "*")
public class RestControllerApi {

    JsonParserService jsonParserService;
    private final DataTransformer dataTransformer;
    SchemaDTO schemaDTO;

    public RestControllerApi(JsonParserService jsonParserService, DataTransformer dataTransformer) {
        this.jsonParserService = jsonParserService;
        this.dataTransformer = dataTransformer;
    }

    @GetMapping
    public HttpStatus loadMapping() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();



            return HttpStatus.OK;
        } catch (Exception e) {
            return HttpStatus.BAD_REQUEST;
        }
    }

    @PostMapping
    public HttpStatus saveMapping(@RequestBody String body) {
        try {
            System.out.println("Received data: " + body);

            // Получаем пользователя из SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            System.out.println("User: " + username + " is saving mapping");

            schemaDTO = jsonParserService.parseAndSave(body);
            return HttpStatus.OK;
        } catch (Exception e) {
            return HttpStatus.BAD_REQUEST;
        }
    }

    @PostMapping("/process")
    public ResponseEntity<String> processMapping(@RequestBody String body) {
        try {
            String result = dataTransformer.transformData(body, schemaDTO);

            // Получаем пользователя
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            System.out.println("User: " + username + " processed mapping");

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error processing: " + e.getMessage());
        }
    }
}
