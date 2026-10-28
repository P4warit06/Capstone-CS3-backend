package com.example.demo.controllers;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.example.demo.services.courses.CoursesService;
import com.example.demo.entities.Course;
import com.example.demo.dtos.PagedResponse;
import java.util.List;

@RestController
@RequestMapping("/v1/courses")
public class CoursesController {

    @Autowired
    private CoursesService coursesService;

    @GetMapping
    public PagedResponse<Course> getAllCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Course> coursePage = coursesService.getAllCourses(pageable);
        return new PagedResponse<>(
                coursePage.getContent(),
                coursePage.getNumber(),
                coursePage.getSize(),
                coursePage.getTotalElements(),
                coursePage.getTotalPages()
        );
    }
    @GetMapping("/{id}")
    public Course getCourseById(@PathVariable Long id) {
        return coursesService.getCourseById(id);
    }
    @PostMapping
    public Course createCourse(@RequestBody Course course) {
        return coursesService.createCourse(course);
    }
    @PutMapping("/{id}")
    public Course updateCourse(@PathVariable Long id, @RequestBody Course course) {
        return coursesService.updateCourse(id, course);
    }
    @DeleteMapping("/{id}")
    public void deleteCourse(@PathVariable Long id) {
        coursesService.deleteCourse(id);
    }
}
