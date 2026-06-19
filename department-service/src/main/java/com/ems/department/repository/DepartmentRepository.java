package com.ems.department.repository;

import com.ems.department.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

/**
 * Exposed at /departments
 * Custom search: GET /departments/search/findByName?name=Engineering
 */
@RepositoryRestResource(collectionResourceRel = "departments", path = "departments")
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    List<Department> findByName(@Param("name") String name);
}
