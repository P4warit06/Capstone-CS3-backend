package com.example.demo.services.courses;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.*;
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
        List<String> tags = course.getCourseTags() == null ? Collections.emptyList() :
                course.getCourseTags().stream()
                        .map(ct -> ct.getTag().getTagName())
                        .toList();

        List<CategoryDto> categories = course.getCurriculumCategoryCourses() == null ? Collections.emptyList() :
                course.getCurriculumCategoryCourses().stream()
                        .map(ccc -> {
                            Category cat = ccc.getCurriculumCategory().getCategory();
                            return new CategoryDto(
                                    cat.getCategoryId() != null ? cat.getCategoryId().longValue() : null,
                                    cat.getCode(),
                                    cat.getCategoryNameTh(),
                                    cat.getCategoryNameEn()
                            );
                        })
                        .toList();

        Long courseIdLong = course.getCourseId() != null ? course.getCourseId().longValue() : null;

        return new CourseDto(
                courseIdLong,
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

    public Course updateCourse(Long id, Course updatedCourse) {
        return courseRepository.findById(Long.valueOf(id)).map(existingCourse -> {
            existingCourse.setCourseCode(updatedCourse.getCourseCode());
            existingCourse.setNameTh(updatedCourse.getNameTh());
            existingCourse.setNameEn(updatedCourse.getNameEn());
            existingCourse.setCredits(updatedCourse.getCredits());
            existingCourse.setDescriptionTh(updatedCourse.getDescriptionTh());
            existingCourse.setDescriptionEn(updatedCourse.getDescriptionEn());
            existingCourse.setIsActive(updatedCourse.getIsActive());
            return courseRepository.save(existingCourse);
        }).orElse(null);
    }

    public void deleteCourse(Long id) {
        courseRepository.deleteById(Long.valueOf(id));
    }
}