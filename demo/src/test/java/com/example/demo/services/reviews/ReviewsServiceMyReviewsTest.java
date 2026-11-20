package com.example.demo.services.reviews;

import com.example.demo.dtos.MyReviewDto;
import com.example.demo.dtos.ReviewableCourseDto;
import com.example.demo.entity.Course;
import com.example.demo.entity.RegistrarEnrollment;
import com.example.demo.entity.RegistrarStudent;
import com.example.demo.entity.Review;
import com.example.demo.entity.ReviewStatus;
import com.example.demo.entity.User;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.RegistrarEnrollmentRepository;
import com.example.demo.repository.RegistrarStudentRepository;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** SCRUM-244: วิชาที่รีวิวได้ + รีวิวของตัวเองในหน้าโปรไฟล์ */
@ExtendWith(MockitoExtension.class)
class ReviewsServiceMyReviewsTest {

    private static final String EMAIL = "student@ad.sit.kmutt.ac.th";

    @Mock private ReviewRepository reviewRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private UserRepository userRepository;
    @Mock private RegistrarStudentRepository registrarStudentRepository;
    @Mock private RegistrarEnrollmentRepository registrarEnrollmentRepository;

    @InjectMocks private ReviewsService reviewsService;

    private User user;
    private RegistrarStudent student;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(7);
        user.setStudentId("67130500001");

        // เข้า 2567 ตอนนี้ปี 2 เทอม 1 → ปีการศึกษาปัจจุบัน 2568
        student = new RegistrarStudent();
        student.setStudentId("67130500001");
        student.setAdmissionYear(2567);
        student.setCurrentYear(2);
        student.setCurrentSemester(1);
    }

    @Test
    void reviewableCoursesExcludeReviewedAndShowRetakesOnce() {
        Course int101 = course(1, "INT101");
        Course int102 = course(2, "INT102");
        Course gen101 = course(3, "GEN101");

        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(user));
        when(registrarStudentRepository.findByStudentId("67130500001")).thenReturn(Optional.of(student));
        // รีวิว GEN101 ไปแล้ว
        when(reviewRepository.findByUserIdOrderByCreatedAtDescReviewIdDesc(7))
                .thenReturn(List.of(review(10, gen101, ReviewStatus.visible)));
        // INT101 ลงซ้ำ 2 ครั้ง (ปี 1 เทอม 1 และ 2)
        when(registrarEnrollmentRepository.findRegisteredEnrollmentsBeforeTerm("67130500001", 2568, 1))
                .thenReturn(List.of(
                        enrollment(1, 2567, 1),
                        enrollment(2, 2567, 1),
                        enrollment(3, 2567, 1),
                        enrollment(1, 2567, 2)));
        when(courseRepository.findAllById(any())).thenReturn(List.of(int101, int102));

        List<ReviewableCourseDto> result = reviewsService.getReviewableCourses(EMAIL);

        assertEquals(2, result.size());
        // เทอมล่าสุดก่อน: INT101 (ปี 1 เทอม 2) แล้ว INT102 (ปี 1 เทอม 1)
        assertEquals("INT101", result.get(0).courseCode());
        assertEquals(1, result.get(0).studyYear());
        assertEquals(2, result.get(0).semester());
        assertEquals("INT102", result.get(1).courseCode());
        verify(registrarEnrollmentRepository).findRegisteredEnrollmentsBeforeTerm("67130500001", 2568, 1);
    }

    @Test
    void reviewableCoursesAreEmptyForUsersWithoutRegistrarRecord() {
        user.setStudentId(null);
        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(user));

        assertTrue(reviewsService.getReviewableCourses(EMAIL).isEmpty());
        verifyNoInteractions(registrarEnrollmentRepository);
    }

    @Test
    void reviewableCoursesAreEmptyWhenNoCompletedTerm() {
        student.setCurrentYear(1);
        student.setCurrentSemester(1);
        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(user));
        when(registrarStudentRepository.findByStudentId("67130500001")).thenReturn(Optional.of(student));
        when(reviewRepository.findByUserIdOrderByCreatedAtDescReviewIdDesc(7)).thenReturn(List.of());
        when(registrarEnrollmentRepository.findRegisteredEnrollmentsBeforeTerm("67130500001", 2567, 1))
                .thenReturn(List.of());

        assertTrue(reviewsService.getReviewableCourses(EMAIL).isEmpty());
    }

    @Test
    void myReviewsIncludeHiddenStatus() {
        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(user));
        when(reviewRepository.findByUserIdOrderByCreatedAtDescReviewIdDesc(7)).thenReturn(List.of(
                review(11, course(1, "INT101"), ReviewStatus.hidden),
                review(10, course(3, "GEN101"), ReviewStatus.visible)));

        List<MyReviewDto> result = reviewsService.getMyReviews(EMAIL);

        assertEquals(2, result.size());
        assertEquals("INT101", result.get(0).courseCode());
        assertEquals("hidden", result.get(0).status());
        assertEquals("visible", result.get(1).status());
    }

    private static Course course(int id, String code) {
        Course course = new Course();
        course.setCourseId(id);
        course.setCourseCode(code);
        course.setNameTh(code + " TH");
        course.setNameEn(code + " EN");
        course.setCredits(3);
        return course;
    }

    private static RegistrarEnrollment enrollment(int courseId, int academicYear, int semester) {
        RegistrarEnrollment enrollment = new RegistrarEnrollment();
        enrollment.setCourseId(courseId);
        enrollment.setAcademicYear(academicYear);
        enrollment.setSemester(semester);
        enrollment.setStatus("registered");
        return enrollment;
    }

    private static Review review(int id, Course course, ReviewStatus status) {
        Review review = new Review();
        review.setReviewId(id);
        review.setUserId(7);
        review.setCourse(course);
        review.setOverallRating(4);
        review.setDifficultyRating(3);
        review.setWorkloadRating(2);
        review.setStatus(status);
        review.setCreatedAt(LocalDateTime.of(2026, 11, 1, 10, 0));
        return review;
    }
}
