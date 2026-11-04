package com.example.demo.repository;

import com.example.demo.entity.RegistrarEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegistrarEnrollmentRepository extends JpaRepository<RegistrarEnrollment, Integer> {
    List<RegistrarEnrollment> findByStudentId(String studentId);
}
