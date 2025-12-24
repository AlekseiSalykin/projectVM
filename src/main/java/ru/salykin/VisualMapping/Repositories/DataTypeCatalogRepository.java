package ru.salykin.VisualMapping.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.salykin.VisualMapping.Models.DataTypeCatalog;

@Repository
public interface DataTypeCatalogRepository extends JpaRepository<DataTypeCatalog, Long> {
}
