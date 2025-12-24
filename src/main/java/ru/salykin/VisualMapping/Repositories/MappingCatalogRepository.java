package ru.salykin.VisualMapping.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.salykin.VisualMapping.Models.MappingCatalog;

@Repository
public interface MappingCatalogRepository extends JpaRepository<MappingCatalog, Long> {
}
