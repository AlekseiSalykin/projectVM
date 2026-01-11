package ru.salykin.VisualMapping.Models;

import jakarta.persistence.*;

@Entity
@Table(name = "datatype_catalog", schema = "catalog")
public class DataTypeCatalog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "dt_name")
    private String name;

    @Column(name = "dt_schema")
    private String schema;

    // Связь с MappingCatalog через @ManyToOne
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "instruction",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_datatype_instruction")
    )
    private MappingCatalog mappingCatalog;

    @Column(name = "dt_source")
    private boolean source;

    // Геттеры и сеттеры для mappingCatalog
    public MappingCatalog getMappingCatalog() {
        return mappingCatalog;
    }

    public void setMappingCatalog(MappingCatalog mappingCatalog) {
        this.mappingCatalog = mappingCatalog;
    }

    // Удаляем getter и setter для instructionId

    // Добавляем удобный метод для получения instructionId из mappingCatalog
    public long getInstructionId() {
        return mappingCatalog != null ? mappingCatalog.getId() : 0;
    }

    // Остальные геттеры и сеттеры остаются без изменений
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public boolean isSource() {
        return source;
    }

    public void setSource(boolean source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return "DataTypeCatalog{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", schema='" + schema + '\'' +
                ", mappingCatalog=" + mappingCatalog +
                ", source=" + source +
                '}';
    }
}