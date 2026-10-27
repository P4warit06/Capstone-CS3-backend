package main.java.com.example.demo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import main.java.com.example.demo.models.courses;
import java.util.List;  
@Repository
public interface CoursesRepository extends JpaRepository<courses, Long> {
    List<courses> findAll();
    courses findById(Long id);
    courses save(courses course);
    void deleteById(Long id);
}
