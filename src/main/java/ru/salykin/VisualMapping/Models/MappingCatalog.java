package ru.salykin.VisualMapping.Models;

import jakarta.persistence.*;

@Entity
@Table(name = "mapping_catalog")
public class MappingCatalog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "instruction")
    private String mappings;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMappings() { return mappings; }
    public void setMappings(String mappings) { this.mappings = mappings; }

    @Override
    public String toString() {
        return "MappingCatalog{" +
                "id=" + id +
                ", mappings='" + mappings + '\'' +
                '}';
    }
}
