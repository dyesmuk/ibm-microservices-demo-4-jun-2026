package com.ems.employee;

import com.ems.employee.entity.Employee;
import com.ems.employee.repository.EmployeeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class EmployeeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmployeeServiceApplication.class, args);
    }

    /** Seed some demo data on startup */
    @Bean
    CommandLineRunner seedData(EmployeeRepository repo) {
        return args -> {
            repo.save(new Employee("Alice", "Johnson", "alice@ems.com", 1L, 1L));
            repo.save(new Employee("Bob",   "Smith",   "bob@ems.com",   1L, 2L));
            repo.save(new Employee("Carol", "Williams","carol@ems.com", 2L, 3L));
            System.out.println("✅ Employee Service: seed data loaded.");
        };
    }
}
