package com.example.demo.services.courses;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.demo.entities.Course;
import com.example.demo.entities.Category;
import com.example.demo.repositories.CourseRepository;
import com.example.demo.dtos.CourseDto;
import com.example.demo.dtos.CategoryDto;

@Service
public class CoursesService {
    @Autowired
    private CourseRepository courseRepository;

    public Page<CourseDto> getAllCourses(Pageable pageable) {
        Page<Course> coursePage = courseRepository.findAll(pageable);
        return coursePage.map(this::mapToCourseDto);
    }

    public CourseDto mapToCourseDto(Course course) {
        List<String> tags = course.getTags() == null ? Collections.emptyList() : course.getTags();

        List<CategoryDto> categories = new ArrayList<>();
        if (course.getCategory() != null) {
            Category cat = course.getCategory();
            categories.add(new CategoryDto(cat.getId(), cat.getCode(), cat.getNameTh(), cat.getNameEn()));
        }

        return new CourseDto(
                course.getId(),
                course.getCourseCode(),
                course.getNameTh(),
                course.getNameEn(),
                course.getCredits(),
                course.getIsActive(),
                tags,
                categories
        );
    }

    public Course getCourseById(Long id) {
        return courseRepository.findById(id).orElse(null);
    }

    public Course createCourse(Course course) {
        return courseRepository.save(course);
    }

    public Course updateCourse(Long id, Course course) {
        return courseRepository.save(course);
    }

    public void deleteCourse(Long id) {
        courseRepository.deleteById(id);
    }
}
