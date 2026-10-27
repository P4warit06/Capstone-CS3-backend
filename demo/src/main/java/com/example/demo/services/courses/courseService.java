package com.example.demo.services.courses;

import org.springframework.stereotype.Service;

@Service
public class courseService {
    @Autowired
    private courseRepository courseRepository;

    public List<courses> getAllCourses() {
        return courseRepository.findAll();
    }
    
    public courses getCourseById(Long id) {
        return courseRepository.findById(id).orElse(null);
    }

    public courses createCourse(courses course) {
        return courseRepository.save(course);
    }
    
    public courses updateCourse(Long id, courses course) {
        return courseRepository.save(course);
    }

    public void deleteCourse(Long id) {
        courseRepository.deleteById(id);
    }   
}
