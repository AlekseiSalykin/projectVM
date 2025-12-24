package ru.salykin.VisualMapping.Services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.springframework.stereotype.Service;
import ru.salykin.VisualMapping.DTO.*;
import ru.salykin.VisualMapping.Models.DataTypeCatalog;
import ru.salykin.VisualMapping.Models.MappingCatalog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DataTransformer {

    private final ObjectMapper objectMapper;
    private final Map<String, Transformer> transformers;

    @FunctionalInterface
    public interface Transformer {
        Object transform(Object value);
    }

    public DataTransformer() {
        this.objectMapper = new ObjectMapper();
        this.transformers = new HashMap<>();
        initializeTransformers();
    }

    private void initializeTransformers() {
        transformers.put("", value -> {
            if (value instanceof String) {
                String fio = (String) value;
                String[] parts = fio.split("\\s+");
                return parts.length > 0 ? parts[0] : "";
            }
            return value;
        });

        transformers.put("", value -> {
            if (value instanceof String) {
                String fio = (String) value;
                String[] parts = fio.split("\\s+");
                return parts.length > 1 ? parts[1] : "";
            }
            return value;
        });

        transformers.put("", value -> {
            if (value instanceof String) {
                String fio = (String) value;
                String[] parts = fio.split("\\s+");
                return parts.length > 2 ? parts[2] : "";
            }
            return value;
        });

        transformers.put("uppercase", value -> {
            if (value instanceof String) return ((String) value).toUpperCase();
            return value;
        });

        transformers.put("lowercase", value -> {
            if (value instanceof String) return ((String) value).toLowerCase();
            return value;
        });

        transformers.put("trim", value -> {
            if (value instanceof String) return ((String) value).trim();
            return value;
        });

        transformers.put("splitFIO", value -> {
            if (value instanceof String) {
                String fio = (String) value;
                String[] parts = fio.split("\\s+");
                Map<String, String> result = new HashMap<>();
                result.put("lastName", parts.length > 0 ? capitalize(parts[0]) : "");
                result.put("firstName", parts.length > 1 ? capitalize(parts[1]) : "");
                result.put("middleName", parts.length > 2 ? capitalize(parts[2]) : "");
                return result;
            }
            return value;
        });

        transformers.put("splitEmail", value -> {
            if (value instanceof String) {
                String email = (String) value;
                int atIndex = email.indexOf('@');
                if (atIndex > 0) {
                    Map<String, String> result = new HashMap<>();
                    String[] nameParts = email.substring(0, atIndex).split("\\.");
                    result.put("firstName", nameParts.length > 0 ? capitalize(nameParts[0]) : "");
                    result.put("lastName", nameParts.length > 1 ? capitalize(nameParts[1]) : "");
                    result.put("domain", email.substring(atIndex + 1));
                    return result;
                }
            }
            return value;
        });

        transformers.put("formatDate", value -> {
            if (value instanceof String) {
                String dateStr = (String) value;
                try {
                    if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                        String[] parts = dateStr.split("-");
                        if (parts.length == 3) {
                            return parts[2] + "." + parts[1] + "." + parts[0];
                        }
                    } else if (dateStr.matches("\\d{2}\\.\\d{2}\\.\\d{4}")) {
                        String[] parts = dateStr.split("\\.");
                        if (parts.length == 3) {
                            return parts[2] + "-" + parts[1] + "-" + parts[0];
                        }
                    }
                } catch (Exception e) {
                }
            }
            return value;
        });

        // Конкатенация
        transformers.put("concat", value -> {
            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) value;
                return map.values().stream()
                        .map(Object::toString)
                        .reduce("", (a, b) -> a + b);
            }
            return value;
        });
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    /**
     * Основной метод для выполнения трансформации
     * @param sourceDataJson исходные данные в JSON
     * @param instruction инструкция маппинга в виде SchemaDTO
     * @return преобразованные данные в JSON
     */
//    public String transformData(String sourceDataJson, SchemaDTO instruction) {
//        try {
//            // 1. Парсим исходные данные
//            JsonNode sourceData = objectMapper.readTree(sourceDataJson);
//
//            // 2. Создаем целевую структуру
//            ObjectNode targetData = objectMapper.createObjectNode();
//
//            // 3. Выполняем все маппинги
//            for (MappingDTO mapping : instruction.getMappings()) {
//                applyMapping(sourceData, targetData, mapping);
//            }
//
//            // 4. Возвращаем результат
//            return objectMapper.writeValueAsString(targetData);
//
//        } catch (Exception e) {
//            throw new RuntimeException("Ошибка при трансформации данных", e);
//        }
//    }

    public String transformData(String sourceDataJson, SchemaDTO instruction) {
        try {
            JsonNode sourceData = objectMapper.readTree(sourceDataJson);
            ObjectNode targetData = objectMapper.createObjectNode();

            for (MappingDTO mapping : instruction.getMappings()) {
                try {
                    Object sourceValue = extractValueFromPath(sourceData, mapping.getSourceField());
                    Object transformedValue = sourceValue;

                    String targetField = mapping.getTargetField();

                    if (targetField.contains("last_name")) {
                        if (sourceValue instanceof String) {
                            String[] parts = ((String) sourceValue).split("\\s+");
                            transformedValue = parts.length > 0 ? parts[0] : "";
                        }
                    } else if (targetField.contains("first_name")) {
                        if (sourceValue instanceof String) {
                            String[] parts = ((String) sourceValue).split("\\s+");
                            transformedValue = parts.length > 1 ? parts[1] : "";
                        }
                    } else if (targetField.contains("date_of_birth")) {
                        if (sourceValue instanceof String) {
                            String dateStr = (String) sourceValue;
                            if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                                String[] parts = dateStr.split("-");
                                transformedValue = parts[2] + "." + parts[1] + "." + parts[0];
                            }
                        }
                    }

                    insertValueToPath(targetData, targetField, transformedValue);
                } catch (Exception e) {
                    System.err.println("Ошибка в маппинге: " + e.getMessage());
                }
            }

            return objectMapper.writeValueAsString(targetData);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при трансформации данных", e);
        }
    }

    private void applyMapping(JsonNode sourceData, ObjectNode targetData, MappingDTO mapping) {
        try {
            Object sourceValue = extractValueFromPath(sourceData, mapping.getSourceField());

            Object transformedValue = applyTransformation(sourceValue, mapping.getTransformation());

            insertValueToPath(targetData, mapping.getTargetField(), transformedValue);

        } catch (PathNotFoundException e) {
            System.err.println("Поле не найдено: " + mapping.getSourceField());
        } catch (Exception e) {
            System.err.println("Ошибка при обработке маппинга: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Object extractValueFromPath(JsonNode node, String path) {
        String jsonPath = convertToJsonPath(path);
        String jsonString = node.toString();

        Configuration conf = Configuration.defaultConfiguration();
        try {
            Object value = JsonPath.using(conf).parse(jsonString).read(jsonPath);

            if (value instanceof net.minidev.json.JSONArray) {
                net.minidev.json.JSONArray array = (net.minidev.json.JSONArray) value;
                if (!array.isEmpty()) {
                    return convertJsonPathValue(array.get(0));
                }
                return null;
            }
            return convertJsonPathValue(value);

        } catch (PathNotFoundException e) {
            throw e;
        }
    }

    private Object convertJsonPathValue(Object value) {
        if (value == null) return null;

        if (value instanceof String ||
                value instanceof Number ||
                value instanceof Boolean) {
            return value;
        }

        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return value.toString();
        }
    }

    private String convertToJsonPath(String path) {
        if (path == null || path.isEmpty()) {
            return "$";
        }

        if (path.startsWith("$")) {
            return path;
        }

        return "$." + path;
    }

    private Object applyTransformation(Object value, String transformation) {
        if (transformation == null || transformation.trim().isEmpty()) {
            return value;
        }

        Transformer transformer = transformers.get(transformation);
        if (transformer != null) {
            Object result = transformer.transform(value);

            if (result instanceof Map) {
                return result;
            }
            return result;
        }

        System.err.println("Преобразование не найдено: " + transformation);
        return value;
    }

    private void insertValueToPath(ObjectNode targetData, String path, Object value) {
        try {
            String jsonPath = convertToJsonPath(path);
            if (jsonPath.equals("$")) {
                if (value instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) value;
                    for (Map.Entry<String, Object> entry : map.entrySet()) {
                        insertSimpleValue(targetData, entry.getKey(), entry.getValue());
                    }
                } else {
                    targetData.put("value", value.toString());
                }
                return;
            }

            String relativePath = jsonPath.substring(2);
            String[] pathParts = relativePath.split("\\.");

            JsonNode currentNode = targetData;

            for (int i = 0; i < pathParts.length - 1; i++) {
                String part = pathParts[i];

                if (!currentNode.has(part)) {
                    ((ObjectNode) currentNode).set(part, objectMapper.createObjectNode());
                }

                currentNode = currentNode.get(part);
            }

            String finalField = pathParts[pathParts.length - 1];

            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> mapValue = (Map<String, Object>) value;

                for (Map.Entry<String, Object> entry : mapValue.entrySet()) {
                    String fieldName;

                    if (finalField.endsWith(".")) {
                        fieldName = finalField + entry.getKey();
                    } else {
                        if (!currentNode.has(finalField)) {
                            ((ObjectNode) currentNode).set(finalField, objectMapper.createObjectNode());
                        }
                        JsonNode finalNode = currentNode.get(finalField);
                        insertSimpleValue((ObjectNode) finalNode, entry.getKey(), entry.getValue());
                        continue;
                    }

                    insertSimpleValue((ObjectNode) currentNode, fieldName, entry.getValue());
                }
            } else {
                insertSimpleValue((ObjectNode) currentNode, finalField, value);
            }

        } catch (Exception e) {
            System.err.println("Ошибка при вставке значения по пути " + path + ": " + e.getMessage());
            throw new RuntimeException("Ошибка вставки значения", e);
        }
    }

    private void insertSimpleValue(ObjectNode node, String fieldName, Object value) {
        if (value instanceof String) {
            node.put(fieldName, (String) value);
        } else if (value instanceof Integer) {
            node.put(fieldName, (Integer) value);
        } else if (value instanceof Long) {
            node.put(fieldName, (Long) value);
        } else if (value instanceof Double) {
            node.put(fieldName, (Double) value);
        } else if (value instanceof Boolean) {
            node.put(fieldName, (Boolean) value);
        } else if (value == null) {
            node.putNull(fieldName);
        } else {
            node.put(fieldName, value.toString());
        }
    }

    public void addTransformer(String name, Transformer transformer) {
        transformers.put(name, transformer);
    }

    public void removeTransformer(String name) {
        transformers.remove(name);
    }

    public List<String> getAvailableTransformations() {
        return new ArrayList<>(transformers.keySet());
    }

    public String transformFromDatabase(String sourceDataJson, Long instructionId,
                                        MappingCatalog mappingCatalog,
                                        List<DataTypeCatalog> structures) {
        try {
            SchemaDTO instruction = new SchemaDTO();

            SourceSchema sourceSchema = new SourceSchema();
            TargetSchema targetSchema = new TargetSchema();

            for (DataTypeCatalog catalog : structures) {
                if (catalog.getName().contains("source") ||
                        catalog.getName().equals(structures.get(0).getName())) {
                    SchemaDTO parsed = objectMapper.readValue(catalog.getSchema(), SchemaDTO.class);
                    sourceSchema = parsed.getSourceSchema();
                } else {
                    SchemaDTO parsed = objectMapper.readValue(catalog.getSchema(), SchemaDTO.class);
                    targetSchema = parsed.getTargetSchema();
                }
            }

            instruction.setSourceSchema(sourceSchema);
            instruction.setTargetSchema(targetSchema);

            List<MappingDTO> mappings = objectMapper.readValue(
                    mappingCatalog.getMappings(),
                    new TypeReference<List<MappingDTO>>() {}
            );
            instruction.setMappings(mappings);

            return transformData(sourceDataJson, instruction);

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при трансформации из базы", e);
        }
    }
}
