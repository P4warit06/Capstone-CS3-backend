package com.example.demo.controllers;

import com.example.demo.dtos.CourseDetailDto;
import com.example.demo.dtos.CourseDto;
import com.example.demo.dtos.PagedResponseDto;
import com.example.demo.entity.Course;
import com.example.demo.services.courses.CoursesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.Set;


@RestController
@RequestMapping("/v1/courses")
public class CoursesController {

    private static final Set<String> ALLOWED_SORTS = Set.of(
            "courseCode,asc", "courseCode,desc", "rating,desc", "reviewCount,desc");

    @Autowired
    private CoursesService coursesService;

    @GetMapping
    public PagedResponseDto<CourseDto> getAllCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "24") int size,
            @RequestParam(defaultValue = "courseCode,asc") String sort,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category) {

        if (!ALLOWED_SORTS.contains(sort)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid sort. Allowed values: courseCode,asc; courseCode,desc; rating,desc; reviewCount,desc");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<CourseDto> coursePage = coursesService.getAllCourses(q, category, sort, pageable);

        // Out-of-bounds page handling
        if (page >= coursePage.getTotalPages() && coursePage.getTotalPages() > 0) {
            return new PagedResponseDto<>(
                    Collections.emptyList(),
                    page,
                    size,
                    coursePage.getTotalElements(),
                    coursePage.getTotalPages()
            );
        }

        return new PagedResponseDto<>(
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
