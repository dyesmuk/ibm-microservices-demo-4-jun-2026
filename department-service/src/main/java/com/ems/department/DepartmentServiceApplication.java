package com.ems.department;

import com.ems.department.entity.Department;
import com.ems.department.entity.Job;
import com.ems.department.repository.DepartmentRepository;
import com.ems.department.repository.JobRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DepartmentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DepartmentServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner seedData(DepartmentRepository deptRepo, JobRepository jobRepo) {
        return args -> {
            deptRepo.save(new Department("Engineering", "Building A"));
            deptRepo.save(new Department("Human Resources", "Building B"));
            deptRepo.save(new Department("Finance", "Building C"));

            jobRepo.save(new Job("Software Engineer",  "Develops software",      60000.0, 120000.0));
            jobRepo.save(new Job("HR Manager",         "Manages HR operations",  50000.0, 90000.0));
            jobRepo.save(new Job("Financial Analyst",  "Analyses finances",      55000.0, 100000.0));

            System.out.println("✅ Department Service: seed data loaded.");
        };
    }
}
