package com.ems.employee.repository;

import com.ems.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

/**
 * Spring Data REST automatically exposes this repository as a full
 * HAL/JSON REST API at /employees without any controller code.
 *
 * Auto-generated endpoints:
 *   GET    /employees          - list all
 *   GET    /employees/{id}     - get one
 *   POST   /employees          - create
 *   PUT    /employees/{id}     - replace
 *   PATCH  /employees/{id}     - partial update
 *   DELETE /employees/{id}     - delete
 *   GET    /employees/search/findByDepartmentId?departmentId=1
 */
@RepositoryRestResource(collectionResourceRel = "employees", path = "employees")
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByDepartmentId(@Param("departmentId") Long departmentId);

    List<Employee> findByJobId(@Param("jobId") Long jobId);
}
