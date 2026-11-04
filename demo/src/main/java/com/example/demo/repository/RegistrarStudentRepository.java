package com.example.demo.repository;

import com.example.demo.entity.RegistrarStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RegistrarStudentRepository extends JpaRepository<RegistrarStudent, String> {
    Optional<RegistrarStudent> findByStudentId(String studentId);
}