package com.example.demo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import com.example.demo.entities.Course;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findAll();
    Course save(Course course);
    void deleteById(Long id);
    Optional<Course> findByCourseCode(String courseCode);
}

