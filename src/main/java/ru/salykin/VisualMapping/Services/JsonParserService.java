package ru.salykin.VisualMapping.Services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import ru.salykin.VisualMapping.DTO.SchemaDTO;
import ru.salykin.VisualMapping.Models.DataTypeCatalog;
import ru.salykin.VisualMapping.Models.MappingCatalog;
import ru.salykin.VisualMapping.Repositories.DataTypeCatalogRepository;
import ru.salykin.VisualMapping.Repositories.MappingCatalogRepository;

import java.util.List;

@Service
public class JsonParserService {

    private DataTypeCatalogRepository dataTypeCatalogRepository;
    private MappingCatalogRepository mappingCatalogRepository;
    private final DataTransformer dataTransformer;

    public JsonParserService(DataTypeCatalogRepository dataTypeCatalogRepository, MappingCatalogRepository mappingCatalogRepository, DataTransformer dataTransformer) {
        this.dataTypeCatalogRepository = dataTypeCatalogRepository;
        this.mappingCatalogRepository = mappingCatalogRepository;
        this.dataTransformer = dataTransformer;
    }

    String sourceData = """
            {
                "User": {
                    "full_name": "Иванов Иван Иванович",
                    "user_dob": "1990-05-15"
                }
            }
            """;

    public SchemaDTO parseAndSave(String jsonString) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        SchemaDTO schemaDTO = mapper.readValue(jsonString, SchemaDTO.class);

        MappingCatalog mappingCatalog = new MappingCatalog();
        mappingCatalog.setMappings(mapper.writeValueAsString(schemaDTO.getMappings()));
        System.out.println(mappingCatalog);
        MappingCatalog savedMappingCatalog = mappingCatalogRepository.save(mappingCatalog);

        DataTypeCatalog sourceCatalog = new DataTypeCatalog();
        sourceCatalog.setName(schemaDTO.getSourceSchema().getName());
        sourceCatalog.setSchema(mapper.writeValueAsString(schemaDTO.getSourceSchema()));
        sourceCatalog.setMappingCatalog(savedMappingCatalog);
        System.out.println(sourceCatalog);

        DataTypeCatalog targetCatalog = new DataTypeCatalog();
        targetCatalog.setName(schemaDTO.getTargetSchema().getName());
        targetCatalog.setSchema(mapper.writeValueAsString(schemaDTO.getTargetSchema()));
        targetCatalog.setMappingCatalog(savedMappingCatalog);
        System.out.println(targetCatalog);

        DataTypeCatalog savedSourceCatalog = dataTypeCatalogRepository.save(sourceCatalog);
        DataTypeCatalog savedTargetCatalog = dataTypeCatalogRepository.save(targetCatalog);

        System.out.println("Successfully saved all entities!");

        return schemaDTO;
    }

//    public List<SchemaDTO> loadAllMappings() {
//
//    }
}
