package main.java.com.example.demo.services.courses;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import main.java.com.example.demo.models.courses;
import main.java.com.example.demo.repositories.CoursesRepository;
@Service
public class CoursesService {
    @Autowired
    private CoursesRepository coursesRepository;

    public List<courses> getAllCourses() {
        return courseRepository.findAll();
    }
    
    public courses getCourseById(Long id) {
        return coursesRepository.findById(id).orElse(null);
    }

    public courses createCourse(courses course) {
        return coursesRepository.save(course);
    }
    
    public courses updateCourse(Long id, courses course) {
        return coursesRepository.save(course);
    }

    public void deleteCourse(Long id) {
        courseRepository.deleteById(id);
    }   
}
