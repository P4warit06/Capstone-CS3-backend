package main.java.com.example.demo.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

@RestController
@RequestMapping("/api/v1/courses")
public class CoursesController {

    @GetMapping("/courses")
    public List<Course> getAllCourses() {
        return courseService.getAllCourses();
    }
    @GetMapping("/courses/{id}")
    public courses getCourseById(@PathVariable Long id) {
        return courseService.getCourseById(id);
    }
    @PostMapping("/courses")
    public courses createCourse(@RequestBody courses course) {
        return courseService.createCourse(course);
    }
    @PutMapping("/courses/{id}")
    public courses updateCourse(@PathVariable Long id, @RequestBody courses course) {   
        return courseService.updateCourse(id, course);
    }
    @DeleteMapping("/courses/{id}")
    public void deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
    }
}
