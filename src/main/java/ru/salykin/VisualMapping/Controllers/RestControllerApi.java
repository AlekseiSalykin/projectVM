package ru.salykin.VisualMapping.Controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.salykin.VisualMapping.DTO.SchemaDTO;
import ru.salykin.VisualMapping.DTO.Token;
import ru.salykin.VisualMapping.DTO.UserDTO;
import ru.salykin.VisualMapping.Services.DataTransformer;
import ru.salykin.VisualMapping.Utils.JsonParserService;

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
    public HttpStatus testMethod(@RequestHeader("api-key") String key) {
        if (key.equals("123")) {
            return HttpStatus.OK;
        }
        return HttpStatus.UNAUTHORIZED;
    }

    @PostMapping
    public HttpStatus testPostRequest(@RequestBody String body) {
        try {
            System.out.println(body);
            schemaDTO = jsonParserService.parseAndSave(body);
            return HttpStatus.OK;
        } catch (Exception e) {
            return HttpStatus.BAD_REQUEST;
        }
    }

    @PostMapping("/process")
    public ResponseEntity<String> processMapping(@RequestBody String body) {
        String result = dataTransformer.transformData(body, schemaDTO);

        return ResponseEntity.ok(result);
    }
}
