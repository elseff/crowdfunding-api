package ru.elseff.crowdfoundingapi.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.elseff.crowdfoundingapi.dao.entity.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
}
