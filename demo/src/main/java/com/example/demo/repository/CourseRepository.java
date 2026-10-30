package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import com.example.demo.entity.Course;
import java.util.Optional;

@Repository 
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findAll();
    Course save(Course course);
    void deleteById(Long id);
    Optional<Course> findByCourseCode(String courseCode);
}

