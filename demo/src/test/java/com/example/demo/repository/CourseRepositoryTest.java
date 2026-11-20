package com.example.demo.repository;

import com.example.demo.entity.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:course-sort-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:course-sort-schema.sql"
})
@Transactional
class CourseRepositoryTest {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private EntityManager entityManager;

    private CurriculumVersion curriculumVersion;

    @Test
    void ratingSortIgnoresHiddenReviewsAndPlacesUnratedCoursesLast() {
        Course noReview = saveCourse("AAA", "other course");
        Course hiddenOnly = saveCourse("BBB", "other course");
        Course visibleFour = saveCourse("CCC", "data course");
        Course visibleFive = saveCourse("DDD", "data course");

        saveReview(hiddenOnly, 5, ReviewStatus.hidden);
        saveReview(visibleFour, 4, ReviewStatus.visible);
        saveReview(visibleFour, 4, ReviewStatus.visible);
        saveReview(visibleFour, 4, ReviewStatus.visible);
        saveReview(visibleFive, 5, ReviewStatus.visible);
        entityManager.flush();

        Page<Course> result = query(null, false, List.of("__NO_CATEGORY__"), "rating,desc", 0, 10);

        assertEquals(List.of("DDD", "CCC", "AAA", "BBB"),
                result.getContent().stream().map(Course::getCourseCode).toList());
        Page<Course> countSorted = query(null, false, List.of("__NO_CATEGORY__"), "reviewCount,desc", 0, 10);
        assertEquals(List.of("CCC", "DDD", "AAA", "BBB"),
                countSorted.getContent().stream().map(Course::getCourseCode).toList());
        List<ReviewRepository.CourseReviewStatsView> stats = reviewRepository.getStatsByCourseIds(
                List.of(noReview.getCourseId(), hiddenOnly.getCourseId()), ReviewStatus.visible);
        assertEquals(List.of(), stats);
    }

    @Test
    void combinesSearchAndCategoryFiltersAndKeepsRatingTiesStableAcrossPages() {
        Category category = saveCategory("GE");
        Course first = saveCourse("DATA101", "data structures");
        Course second = saveCourse("DATA102", "data systems");
        Course third = saveCourse("DATA103", "data science");
        Course wrongCategory = saveCourse("DATA104", "data fundamentals");
        addToCategory(first, category);
        addToCategory(second, category);
        addToCategory(third, category);
        saveReview(first, 4, ReviewStatus.visible);
        saveReview(second, 4, ReviewStatus.visible);
        saveReview(third, 4, ReviewStatus.visible);
        saveReview(wrongCategory, 5, ReviewStatus.visible);
        entityManager.flush();

        Page<Course> pageOne = query("data", true, List.of("GE"), "rating,desc", 0, 2);
        Page<Course> pageTwo = query("data", true, List.of("GE"), "rating,desc", 1, 2);

        assertEquals(List.of("DATA101", "DATA102"),
                pageOne.getContent().stream().map(Course::getCourseCode).toList());
        assertEquals(List.of("DATA103"),
                pageTwo.getContent().stream().map(Course::getCourseCode).toList());
        assertEquals(3, pageOne.getTotalElements());
        assertEquals(3, pageTwo.getTotalElements());
    }

    private Page<Course> query(String q, boolean hasCodes, List<String> codes, String sort, int page, int size) {
        return courseRepository.findCourses(q == null ? "" : q, q != null, hasCodes, codes, sort, ReviewStatus.visible,
                PageRequest.of(page, size));
    }

    private Course saveCourse(String code, String name) {
        LocalDateTime now = LocalDateTime.now();
        Course course = new Course();
        course.setCourseCode(code);
        course.setNameTh(name);
        course.setNameEn(name);
        course.setCredits(3);
        course.setIsActive(true);
        course.setCreatedAt(now);
        course.setUpdatedAt(now);
        return courseRepository.save(course);
    }

    private void saveReview(Course course, int rating, ReviewStatus status) {
        User user = new User();
        user.setEmail("user" + System.nanoTime() + "@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setDisplayName("Test User");
        user.setRole(UserRole.student);
        entityManager.persist(user);

        Review review = new Review();
        review.setUserId(user.getId());
        review.setCourse(course);
        review.setOverallRating(rating);
        review.setDifficultyRating(rating);
        review.setWorkloadRating(rating);
        review.setStatus(status);
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());
        reviewRepository.save(review);
    }

    private Category saveCategory(String code) {
        LocalDateTime now = LocalDateTime.now();
        Category category = new Category();
        category.setCode(code);
        category.setCategoryNameTh(code);
        category.setCategoryNameEn(code);
        category.setCreatedAt(now);
        category.setUpdatedAt(now);
        entityManager.persist(category);
        return category;
    }

    private void addToCategory(Course course, Category category) {
        LocalDateTime now = LocalDateTime.now();
        if (curriculumVersion == null) {
            curriculumVersion = new CurriculumVersion();
            curriculumVersion.setVersionName("Test curriculum");
            curriculumVersion.setAcademicYear(2025);
            curriculumVersion.setTotalCredits(120);
            curriculumVersion.setIsActive(true);
            curriculumVersion.setCreatedAt(now);
            curriculumVersion.setUpdatedAt(now);
            entityManager.persist(curriculumVersion);
        }

        CurriculumCategory curriculumCategory = new CurriculumCategory();
        curriculumCategory.setCurriculumVersion(curriculumVersion);
        curriculumCategory.setCategory(category);
        curriculumCategory.setRequiredCredits(3);
        curriculumCategory.setCreatedAt(now);
        curriculumCategory.setUpdatedAt(now);
        entityManager.persist(curriculumCategory);

        CurriculumCategoryCourse courseCategory = new CurriculumCategoryCourse();
        courseCategory.setCourse(course);
        courseCategory.setCurriculumCategory(curriculumCategory);
        entityManager.persist(courseCategory);
    }
}
