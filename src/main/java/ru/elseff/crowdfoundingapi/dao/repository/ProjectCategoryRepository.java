package ru.elseff.crowdfoundingapi.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.elseff.crowdfoundingapi.dao.entity.ProjectCategory;

import java.util.Optional;

@Repository
public interface ProjectCategoryRepository extends JpaRepository<ProjectCategory, Long> {
    Optional<ProjectCategory> findByName(String name);
}
