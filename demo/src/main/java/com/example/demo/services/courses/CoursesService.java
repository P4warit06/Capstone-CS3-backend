package com.example.demo.services.courses;

import com.example.demo.dtos.CategoryDto;
import com.example.demo.dtos.CourseDetailDto;
import com.example.demo.dtos.CourseDto;
import com.example.demo.entity.Category;
import com.example.demo.entity.Course;
import com.example.demo.entity.ReviewStatus;
import com.example.demo.exceptions.CourseNotFoundException;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CoursesService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    public Page<CourseDto> getAllCourses(Pageable pageable) {
        return getAllCourses(null, null, "courseCode,asc", pageable);
    }

    /** q ว่าง = ทั้งหมด, มีค่า = ค้นจากรหัส / ชื่อไทย / ชื่ออังกฤษ */
    public Page<CourseDto> getAllCourses(String q, Pageable pageable) {
        return getAllCourses(q, null, "courseCode,asc", pageable);
    }

    /**
     * SCRUM-218: category = code ของหมวด คั่นด้วย comma เช่น "GE,FREE_ELECTIVE" (หลายหมวด = OR)
     * ว่าง/null = ไม่ filter หมวด
     */
    public Page<CourseDto> getAllCourses(String q, String category, Pageable pageable) {
        return getAllCourses(q, category, "courseCode,asc", pageable);
    }

    public Page<CourseDto> getAllCourses(String q, String category, String sort, Pageable pageable) {
        boolean hasQuery = q != null && !q.isBlank();
        String query = hasQuery ? q.trim() : "";
        List<String> codes = parseCategoryCodes(category);
        List<String> queryCodes = codes.isEmpty() ? List.of("__NO_CATEGORY__") : codes;
        Pageable unsortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        Page<Course> coursePage = courseRepository.findCourses(
                query, hasQuery, !codes.isEmpty(), queryCodes, sort, ReviewStatus.visible, unsortedPageable);
        Map<Integer, ReviewRepository.CourseReviewStatsView> reviewStatsByCourseId = getReviewStats(coursePage.getContent());
        return coursePage.map(course -> mapToCourseDto(course, reviewStatsByCourseId.get(course.getCourseId())));
    }

    private List<String> parseCategoryCodes(String category) {
        if (category == null || category.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(category.split(","))
                .map(String::trim)
                .filter(code -> !code.isEmpty())
                .map(code -> code.toUpperCase(Locale.ROOT))
                .distinct()
                .toList();
    }

    public CourseDto mapToCourseDto(Course course) {
        Map<Integer, ReviewRepository.CourseReviewStatsView> reviewStatsByCourseId = getReviewStats(List.of(course));
        return mapToCourseDto(course, reviewStatsByCourseId.get(course.getCourseId()));
    }

    public List<CourseDto> mapToCourseDtos(List<Course> courses) {
        Map<Integer, ReviewRepository.CourseReviewStatsView> reviewStatsByCourseId = getReviewStats(courses);
        return courses.stream()
                .map(course -> mapToCourseDto(course, reviewStatsByCourseId.get(course.getCourseId())))
                .toList();
    }

    private Map<Integer, ReviewRepository.CourseReviewStatsView> getReviewStats(List<Course> courses) {
        List<Integer> courseIds = courses.stream()
                .map(Course::getCourseId)
                .filter(Objects::nonNull)
                .toList();
        if (courseIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Integer, ReviewRepository.CourseReviewStatsView> reviewStatsByCourseId = new HashMap<>();
        reviewRepository.getStatsByCourseIds(courseIds, ReviewStatus.visible)
                .forEach(stats -> reviewStatsByCourseId.put(stats.getCourseId(), stats));
        return reviewStatsByCourseId;
    }

    private CourseDto mapToCourseDto(Course course, ReviewRepository.CourseReviewStatsView reviewStats) {
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
                reviewStats == null ? null : round1(reviewStats.getAverageRating()),
                reviewStats == null || reviewStats.getReviewCount() == null ? 0L : reviewStats.getReviewCount(),
                categories
        );
    }

    public CourseDetailDto getCourseByCode (String courseCode) {
        Course course = courseRepository.findByCourseCode(courseCode)
                .orElseThrow(() -> new CourseNotFoundException("Course with code '" + courseCode + "' not found"));
        return mapToCourseDetailDto(course);
    }

    private CourseDetailDto mapToCourseDetailDto(Course course) {
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

        return new CourseDetailDto(
                courseIdLong,
                course.getCourseCode(),
                course.getNameTh(),
                course.getNameEn(),
                course.getCredits(),
                course.getDescriptionTh(),
                course.getDescriptionEn(),
                course.getIsActive(),
                tags,
                categories,
                course.getCreatedAt(),
                course.getUpdatedAt()
        );
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

    private static Double round1(Double value) {
        return value == null ? null : Math.round(value * 10) / 10.0;
    }
}
