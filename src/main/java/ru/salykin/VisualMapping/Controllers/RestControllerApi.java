package ru.salykin.VisualMapping.Controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ru.salykin.VisualMapping.DTO.SchemaDTO;
import ru.salykin.VisualMapping.Models.DataTypeCatalog;
import ru.salykin.VisualMapping.Models.MappingCatalog;
import ru.salykin.VisualMapping.Repositories.DataTypeCatalogRepository;
import ru.salykin.VisualMapping.Repositories.MappingCatalogRepository;
import ru.salykin.VisualMapping.Services.DataTransformer;
import ru.salykin.VisualMapping.Services.JsonParserService;

import java.util.*;

@RestController
@RequestMapping("/apimapping")
@CrossOrigin(origins = "*")
public class RestControllerApi {

    JsonParserService jsonParserService;
    private final DataTransformer dataTransformer;
    SchemaDTO schemaDTO;
    private final DataTypeCatalogRepository dataTypeCatalogRepository;
    private final MappingCatalogRepository mappingCatalogRepository;
    private final ObjectMapper objectMapper;

    public RestControllerApi(JsonParserService jsonParserService, DataTransformer dataTransformer, DataTypeCatalogRepository dataTypeCatalogRepository, MappingCatalogRepository mappingCatalogRepository, ObjectMapper objectMapper) {
        this.jsonParserService = jsonParserService;
        this.dataTransformer = dataTransformer;
        this.dataTypeCatalogRepository = dataTypeCatalogRepository;
        this.mappingCatalogRepository = mappingCatalogRepository;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/loadListMapping")
    public ResponseEntity<List<Map<String, Object>>> loadMapping() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            List<MappingCatalog> mappings = mappingCatalogRepository.findAll();

            List<Map<String, Object>> result = new ArrayList<>();

            for (MappingCatalog mapping : mappings) {
                Map<String, Object> mappingData = new HashMap<>();

                List<DataTypeCatalog> catalogTypes = dataTypeCatalogRepository.findByMappingCatalogId(mapping.getId());

                DataTypeCatalog sourceSchema = null;
                DataTypeCatalog targetSchema = null;

                for (DataTypeCatalog dt : catalogTypes) {
                    if (dt.isSource()){
                        sourceSchema = dt;
                    } else {
                        targetSchema = dt;
                    }
                }

                Map<String, Object> combined = new HashMap<>();

                if (sourceSchema != null) {
                    try {
                        combined.put("sourceSchema", objectMapper.readValue(sourceSchema.getSchema(), Map.class));
                    } catch (JsonProcessingException e) {
                        combined.put("sourceSchema", Collections.emptyMap());
                    }
                } else {
                    combined.put("sourceSchema", Collections.emptyMap());
                }

                if (targetSchema != null) {
                    try {
                        combined.put("targetSchema", objectMapper.readValue(targetSchema.getSchema(), Map.class));
                    } catch (JsonProcessingException e) {
                        combined.put("targetSchema", Collections.emptyMap());
                    }
                } else {
                    combined.put("targetSchema", Collections.emptyMap());
                }

                try {
                    combined.put("mappings", objectMapper.readValue(mapping.getMappings(), List.class));
                } catch (JsonProcessingException e) {
                    combined.put("mappings", Collections.emptyList());
                }

                mappingData.put("id", mapping.getId());
                mappingData.put("name", sourceSchema != null ? sourceSchema.getName() + " → " +
                        (targetSchema != null ? targetSchema.getName() : "Unknown") : "Unnamed Mapping");
                mappingData.put("mappings", objectMapper.writeValueAsString(combined));

                result.add(mappingData);

                for (DataTypeCatalog catalogType : catalogTypes) {
                    System.out.println(catalogType);
                }
                System.out.println("------------------------");
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Collections.emptyList());
        }
    }

    @GetMapping("/{id}/combined")
    public ResponseEntity<Map<String, Object>> getCombinedMapping(@PathVariable Long id) {
        try {
            Optional<MappingCatalog> mappingOpt = mappingCatalogRepository.findById(id);
            if (mappingOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            MappingCatalog mapping = mappingOpt.get();
            List<DataTypeCatalog> dataTypes = dataTypeCatalogRepository.findByMappingCatalogId(id);

            Map<String, Object> combined = new HashMap<>();

            DataTypeCatalog sourceSchema = null;
            DataTypeCatalog targetSchema = null;

            for (DataTypeCatalog dt : dataTypes) {
                if (dt.isSource()){
                    sourceSchema = dt;
                } else {
                    targetSchema = dt;
                }
            }

            if (sourceSchema != null) {
                try {
                    combined.put("sourceSchema", objectMapper.readValue(sourceSchema.getSchema(), Map.class));
                } catch (JsonProcessingException e) {
                    combined.put("sourceSchema", Collections.emptyMap());
                }
            } else {
                combined.put("sourceSchema", Collections.emptyMap());
            }

            if (targetSchema != null) {
                try {
                    combined.put("targetSchema", objectMapper.readValue(targetSchema.getSchema(), Map.class));
                } catch (JsonProcessingException e) {
                    combined.put("targetSchema", Collections.emptyMap());
                }
            } else {
                combined.put("targetSchema", Collections.emptyMap());
            }
            try {
                combined.put("mappings", objectMapper.readValue(mapping.getMappings(), List.class));
            } catch (JsonProcessingException e) {
                combined.put("mappings", Collections.emptyList());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("id", mapping.getId());
            response.put("mappings", objectMapper.writeValueAsString(combined));
            System.out.println(response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
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
