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

import java.util.List;

@Service
public class DataTransformer {

    private final ObjectMapper objectMapper;

    public DataTransformer() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Основной метод для выполнения простого маппинга без преобразований
     * @param sourceDataJson исходные данные в JSON
     * @param instruction инструкция маппинга в виде SchemaDTO
     * @return преобразованные данные в JSON
     */
    public String transformData(String sourceDataJson, SchemaDTO instruction) {
        try {
            // 1. Парсим исходные данные
            JsonNode sourceData = objectMapper.readTree(sourceDataJson);

            // 2. Создаем целевую структуру
            ObjectNode targetData = objectMapper.createObjectNode();

            // 3. Выполняем все маппинги (простое копирование значений)
            for (MappingDTO mapping : instruction.getMappings()) {
                try {
                    // Извлекаем значение из исходных данных
                    Object sourceValue = extractValueFromPath(sourceData, mapping.getSourceField());

                    // Вставляем значение в целевую структуру (без преобразований)
                    insertValueToPath(targetData, mapping.getTargetField(), sourceValue);

                } catch (PathNotFoundException e) {
                    // Поле не найдено в исходных данных - пропускаем этот маппинг
                    System.err.println("Поле не найдено: " + mapping.getSourceField());
                } catch (Exception e) {
                    // Логируем другие ошибки, но продолжаем обработку остальных маппингов
                    System.err.println("Ошибка при обработке маппинга " + mapping.getSourceField() +
                            " -> " + mapping.getTargetField() + ": " + e.getMessage());
                }
            }

            // 4. Возвращаем результат в виде JSON-строки
            return objectMapper.writeValueAsString(targetData);

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при выполнении маппинга данных", e);
        }
    }

    /**
     * Извлекает значение из JSON по указанному пути
     */
    private Object extractValueFromPath(JsonNode node, String path) {
        try {
            String jsonPath = convertToJsonPath(path);
            String jsonString = node.toString();

            Configuration conf = Configuration.defaultConfiguration();
            Object value = JsonPath.using(conf).parse(jsonString).read(jsonPath);

            // Обработка массивов - берем первый элемент, если это массив
            if (value instanceof net.minidev.json.JSONArray) {
                net.minidev.json.JSONArray array = (net.minidev.json.JSONArray) value;
                if (!array.isEmpty()) {
                    return convertJsonPathValue(array.get(0));
                }
                return null;
            }

            return convertJsonPathValue(value);

        } catch (PathNotFoundException e) {
            throw e; // Пробрасываем исключение для обработки на верхнем уровне
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при извлечении значения по пути: " + path, e);
        }
    }

    /**
     * Конвертирует значение из JsonPath в Java-объект
     */
    private Object convertJsonPathValue(Object value) {
        if (value == null) {
            return null;
        }

        // Возвращаем примитивные типы как есть
        if (value instanceof String ||
                value instanceof Number ||
                value instanceof Boolean) {
            return value;
        }

        // Для сложных объектов возвращаем строковое представление
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return value.toString();
        }
    }

    /**
     * Преобразует путь в формат JsonPath
     */
    private String convertToJsonPath(String path) {
        if (path == null || path.isEmpty()) {
            return "$";
        }

        if (path.startsWith("$")) {
            return path; // Уже в формате JsonPath
        }

        return "$." + path; // Добавляем корневой элемент
    }

    /**
     * Вставляет значение в JSON по указанному пути
     */
    private void insertValueToPath(ObjectNode targetData, String path, Object value) {
        try {
            String jsonPath = convertToJsonPath(path);

            // Обработка корневого пути
            if (jsonPath.equals("$")) {
                targetData.put("value", value != null ? value.toString() : "null");
                return;
            }

            // Убираем "$." из начала пути
            String relativePath = jsonPath.substring(2);
            String[] pathParts = relativePath.split("\\.");

            JsonNode currentNode = targetData;

            // Создаем вложенную структуру до последнего элемента пути
            for (int i = 0; i < pathParts.length - 1; i++) {
                String part = pathParts[i];

                if (!currentNode.has(part)) {
                    ((ObjectNode) currentNode).set(part, objectMapper.createObjectNode());
                }

                currentNode = currentNode.get(part);
            }

            // Вставляем значение в последний элемент пути
            String finalField = pathParts[pathParts.length - 1];
            insertSimpleValue((ObjectNode) currentNode, finalField, value);

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при вставке значения по пути: " + path, e);
        }
    }

    /**
     * Вставляет простое значение в JsonNode
     */
    private void insertSimpleValue(ObjectNode node, String fieldName, Object value) {
        if (value == null) {
            node.putNull(fieldName);
        } else if (value instanceof String) {
            node.put(fieldName, (String) value);
        } else if (value instanceof Integer) {
            node.put(fieldName, (Integer) value);
        } else if (value instanceof Long) {
            node.put(fieldName, (Long) value);
        } else if (value instanceof Double) {
            node.put(fieldName, (Double) value);
        } else if (value instanceof Float) {
            node.put(fieldName, (Float) value);
        } else if (value instanceof Boolean) {
            node.put(fieldName, (Boolean) value);
        } else {
            // Для сложных объектов сохраняем как строку
            node.put(fieldName, value.toString());
        }
    }

    /**
     * Метод для трансформации данных из базы
     */
    public String transformFromDatabase(String sourceDataJson, Long instructionId,
                                        MappingCatalog mappingCatalog,
                                        List<DataTypeCatalog> structures) {
        try {
            SchemaDTO instruction = new SchemaDTO();

            SourceSchema sourceSchema = new SourceSchema();
            TargetSchema targetSchema = new TargetSchema();

            // Определяем исходную и целевую схемы
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

            // Получаем маппинги из базы
            List<MappingDTO> mappings = objectMapper.readValue(
                    mappingCatalog.getMappings(),
                    new TypeReference<List<MappingDTO>>() {}
            );
            instruction.setMappings(mappings);

            // Выполняем маппинг
            return transformData(sourceDataJson, instruction);

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при трансформации из базы данных", e);
        }
    }
}