package com.example.demo.services.courses;

import com.example.demo.dtos.CourseDto;
import com.example.demo.entity.Course;
import com.example.demo.entity.SavedCourse;
import com.example.demo.entity.User;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.SavedCourseRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SavedCourseService {

    private static final String USER_NOT_FOUND = "ไม่พบผู้ใช้";
    private static final String COURSE_NOT_FOUND = "ไม่พบรายวิชา";
    private static final String ALREADY_SAVED = "บันทึกวิชานี้ไว้แล้ว";
    private static final String SAVED_COURSE_NOT_FOUND = "ไม่พบวิชาที่บันทึกไว้";

    private final SavedCourseRepository savedCourseRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CoursesService coursesService;

    @Transactional(readOnly = true)
    public List<CourseDto> getSavedCourses(Integer userId) {
        ensureUserExists(userId);
        return savedCourseRepository
                .findByUser_IdOrderByCreatedAtDescSavedCourseIdDesc(userId)
                .stream()
                .map(SavedCourse::getCourse)
                .map(coursesService::mapToCourseDto)
                .toList();
    }

    @Transactional
    public CourseDto saveCourse(Integer userId, Integer courseId) {
        User user = findUser(userId);
        Course course = courseRepository.findByCourseId(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, COURSE_NOT_FOUND));

        if (savedCourseRepository.existsByUser_IdAndCourse_CourseId(userId, courseId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ALREADY_SAVED);
        }

        try {
            savedCourseRepository.saveAndFlush(new SavedCourse(user, course));
        } catch (DataIntegrityViolationException exception) {
            // ป้องกัน race condition กรณีส่งคำขอบันทึกวิชาเดียวกันพร้อมกัน
            throw new ResponseStatusException(HttpStatus.CONFLICT, ALREADY_SAVED, exception);
        }

        return coursesService.mapToCourseDto(course);
    }

    @Transactional
    public void removeCourse(Integer userId, Integer courseId) {
        ensureUserExists(userId);
        SavedCourse savedCourse = savedCourseRepository
                .findByUser_IdAndCourse_CourseId(userId, courseId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, SAVED_COURSE_NOT_FOUND));
        savedCourseRepository.delete(savedCourse);
    }

    private void ensureUserExists(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND);
        }
    }

    private User findUser(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));
    }
}
