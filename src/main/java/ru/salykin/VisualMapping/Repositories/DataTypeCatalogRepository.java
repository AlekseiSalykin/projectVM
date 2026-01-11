package ru.salykin.VisualMapping.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.salykin.VisualMapping.Models.DataTypeCatalog;

import java.util.List;

@Repository
public interface DataTypeCatalogRepository extends JpaRepository<DataTypeCatalog, Long> {

    @Query("SELECT d FROM DataTypeCatalog d WHERE d.mappingCatalog.id = :instruction")
    List<DataTypeCatalog> findByMappingCatalogId(@Param("instruction") Long mappingId);
}
