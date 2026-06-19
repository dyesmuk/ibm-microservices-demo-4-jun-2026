package com.ems.department.repository;

import com.ems.department.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

/**
 * Exposed at /jobs
 * Custom search: GET /jobs/search/findByTitle?title=Engineer
 */
@RepositoryRestResource(collectionResourceRel = "jobs", path = "jobs")
public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByTitle(@Param("title") String title);
}
