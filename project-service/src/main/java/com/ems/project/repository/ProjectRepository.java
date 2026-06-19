package com.ems.project.repository;

import com.ems.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

/**
 * Exposed at /projects
 * Custom searches:
 *   GET /projects/search/findByStatus?status=ACTIVE
 *   GET /projects/search/findByDepartmentId?departmentId=1
 */
@RepositoryRestResource(collectionResourceRel = "projects", path = "projects")
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByStatus(@Param("status") String status);

    List<Project> findByDepartmentId(@Param("departmentId") Long departmentId);
}
