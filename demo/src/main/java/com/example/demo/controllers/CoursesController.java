package com.example.demo.controllers;
import com.example.demo.dtos.CourseDetailDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.example.demo.services.courses.CoursesService;
import com.example.demo.entity.Course;
import com.example.demo.dtos.CourseDto;
import com.example.demo.dtos.PagedResponse;
import java.util.Collections;


@RestController
@RequestMapping("/v1/courses")
public class CoursesController {

    @Autowired
    private CoursesService coursesService;

    @GetMapping
    public PagedResponse<CourseDto> getAllCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "24") int size,
            @RequestParam(defaultValue = "courseCode,asc") String sort) {

        // Parse sort parameter (e.g., "courseCode,asc")
        String[] sortParts = sort.split(",");
        Sort sorting = Sort.by(sortParts[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortParts[0]);

        Pageable pageable = PageRequest.of(page, size, sorting);
        Page<CourseDto> coursePage = coursesService.getAllCourses(pageable);

        // Out-of-bounds page handling
        if (page >= coursePage.getTotalPages() && coursePage.getTotalPages() > 0) {
            return new PagedResponse<>(
                    Collections.emptyList(),
                    page,
                    size,
                    coursePage.getTotalElements(),
                    coursePage.getTotalPages()
            );
        }

        return new PagedResponse<>(
                coursePage.getContent(),
                coursePage.getNumber(),
                coursePage.getSize(),
                coursePage.getTotalElements(),
                coursePage.getTotalPages()
        );
    }
    @GetMapping("/{courseCode}")
    public ResponseEntity<CourseDetailDto> getCourseByCode(@PathVariable String courseCode) {
        CourseDetailDto course = coursesService.getCourseByCode(courseCode);
        return ResponseEntity.ok(course);
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
