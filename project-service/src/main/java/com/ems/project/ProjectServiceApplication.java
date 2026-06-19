package com.ems.project;

import com.ems.project.entity.Project;
import com.ems.project.repository.ProjectRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;

@SpringBootApplication
public class ProjectServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner seedData(ProjectRepository repo) {
        return args -> {
            repo.save(new Project(
                    "EMS Platform",
                    "Build the internal EMS system",
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 12, 31),
                    "ACTIVE", 1L));

            repo.save(new Project(
                    "HR Automation",
                    "Automate HR workflows",
                    LocalDate.of(2024, 3, 1),
                    LocalDate.of(2024, 9, 30),
                    "PLANNING", 2L));

            repo.save(new Project(
                    "Finance Reporting",
                    "Quarterly finance dashboard",
                    LocalDate.of(2024, 2, 1),
                    LocalDate.of(2024, 6, 30),
                    "COMPLETED", 3L));

            System.out.println("✅ Project Service: seed data loaded.");
        };
    }
}
