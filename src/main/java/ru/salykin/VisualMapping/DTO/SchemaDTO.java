package ru.salykin.VisualMapping.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SchemaDTO {
    @JsonProperty("sourceSchema")
    private SourceSchema sourceSchema;
    @JsonProperty("targetSchema")
    private TargetSchema targetSchema;
    @JsonProperty("mappings")
    private List<MappingDTO> mappings;

    public SourceSchema getSourceSchema() {
        return sourceSchema;
    }

    public void setSourceSchema(SourceSchema sourceSchema) {
        this.sourceSchema = sourceSchema;
    }

    public TargetSchema getTargetSchema() {
        return targetSchema;
    }

    public void setTargetSchema(TargetSchema targetSchema) {
        this.targetSchema = targetSchema;
    }

    public List<MappingDTO> getMappings() {
        return mappings;
    }

    public void setMappings(List<MappingDTO> mappings) {
        this.mappings = mappings;
    }
}
