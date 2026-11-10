package com.example.demo.services.reviews;

import com.example.demo.dtos.CourseReviewsResponseDto;
import com.example.demo.dtos.CreateReviewRequestDto;
import com.example.demo.dtos.MyReviewStatusDto;
import com.example.demo.dtos.PagedResponseDto;
import com.example.demo.dtos.ReviewDto;
import com.example.demo.dtos.ReviewSummaryDto;
import com.example.demo.entity.Course;
import com.example.demo.entity.Review;
import com.example.demo.entity.RegistrarStudent;
import com.example.demo.entity.ReviewStatus;
import com.example.demo.entity.User;
import com.example.demo.exceptions.CourseNotFoundException;
import com.example.demo.exceptions.ReviewException;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.RegistrarEnrollmentRepository;
import com.example.demo.repository.RegistrarStudentRepository;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewsService {

    private static final int MAX_PAGE_SIZE = 50;
    private static final int MAX_COMMENT_LENGTH = 2000;

    private final ReviewRepository reviewRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final RegistrarStudentRepository registrarStudentRepository;
    private final RegistrarEnrollmentRepository registrarEnrollmentRepository;

    /** SCRUM-206: สรุปคะแนน + รายการรีวิว (แบ่งหน้า) ของวิชา นับเฉพาะ status = visible */
    @Transactional(readOnly = true)
    public CourseReviewsResponseDto getCourseReviews(String courseCode, int page, int size, String sort) {
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page ต้องไม่ติดลบ");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "size ต้องอยู่ระหว่าง 1-" + MAX_PAGE_SIZE);
        }
        Sort sorting = toSort(sort);

        Course course = findCourse(courseCode);
        Integer courseId = course.getCourseId();

        Page<Review> reviewPage = reviewRepository.findByCourse_CourseIdAndStatus(
                courseId, ReviewStatus.visible, PageRequest.of(page, size, sorting));

        PagedResponseDto<ReviewDto> reviews = new PagedResponseDto<>(
                reviewPage.getContent().stream().map(ReviewsService::toDto).toList(),
                reviewPage.getNumber(),
                reviewPage.getSize(),
                reviewPage.getTotalElements(),
                reviewPage.getTotalPages()
        );
        return new CourseReviewsResponseDto(buildSummary(courseId), reviews);
    }

    /** SCRUM-203: เขียนรีวิวใหม่ — เคยเรียนจริง + ยังไม่เคยรีวิววิชานี้ */
    @Transactional
    public ReviewDto createReview(String email, String courseCode, CreateReviewRequestDto request) {
        String comment = validateAndNormalize(request);
        Course course = findCourse(courseCode);
        User user = findUser(email);

        String reason = ineligibleReason(user, course);
        if (ReviewException.ALREADY_REVIEWED.equals(reason)) {
            throw new ReviewException(HttpStatus.CONFLICT, reason, "คุณรีวิววิชานี้ไปแล้ว");
        }
        if (ReviewException.NOT_ENROLLED.equals(reason)) {
            throw new ReviewException(HttpStatus.FORBIDDEN, reason, "รีวิวได้เฉพาะวิชาที่เคยเรียนจบเทอมแล้ว");
        }

        Review review = new Review();
        review.setUserId(user.getId());
        review.setUser(user);
        review.setCourse(course);
        review.setOverallRating(request.overallRating());
        review.setDifficultyRating(request.difficultyRating());
        review.setWorkloadRating(request.workloadRating());
        review.setComment(comment);
        review.setStatus(ReviewStatus.visible);

        try {
            return toDto(reviewRepository.saveAndFlush(review));
        } catch (DataIntegrityViolationException e) {
            // กดส่งซ้ำพร้อมกัน — unique (user_id, course_id) ใน DB กันไว้
            throw new ReviewException(HttpStatus.CONFLICT, ReviewException.ALREADY_REVIEWED, "คุณรีวิววิชานี้ไปแล้ว");
        }
    }

    /** SCRUM-203: ให้ FE รู้ว่าจะแสดงปุ่ม "เขียนรีวิว" ไหม (ใช้กติกาเดียวกับ createReview) */
    @Transactional(readOnly = true)
    public MyReviewStatusDto getMyReviewStatus(String email, String courseCode) {
        Course course = findCourse(courseCode);
        User user = findUser(email);
        String reason = ineligibleReason(user, course);
        ReviewDto myReview = reviewRepository.findByUserIdAndCourse_CourseId(user.getId(), course.getCourseId())
                .map(ReviewsService::toDto)
                .orElse(null);
        return new MyReviewStatusDto(reason == null, reason, myReview);
    }

    /** null = รีวิวได้ */
    private String ineligibleReason(User user, Course course) {
        if (reviewRepository.existsByUserIdAndCourse_CourseId(user.getId(), course.getCourseId())) {
            return ReviewException.ALREADY_REVIEWED;
        }
        if (user.getStudentId() == null) {
            return ReviewException.NOT_ENROLLED;
        }
        RegistrarStudent student = registrarStudentRepository.findByStudentId(user.getStudentId()).orElse(null);
        if (student == null) {
            return ReviewException.NOT_ENROLLED;
        }
        // ปีการศึกษาปัจจุบัน เช่น เข้า 2567 ตอนนี้ปี 3 → 2569 (นับเฉพาะเทอมก่อนหน้านี้)
        int currentAcademicYear = student.getAdmissionYear() + student.getCurrentYear() - 1;
        boolean enrolled = registrarEnrollmentRepository.existsCompletedEnrollment(
                user.getStudentId(), course.getCourseId(), currentAcademicYear, student.getCurrentSemester());
        return enrolled ? null : ReviewException.NOT_ENROLLED;
    }

    /** คืน comment ที่ trim แล้ว (ว่าง = null) */
    private static String validateAndNormalize(CreateReviewRequestDto request) {
        if (request == null) {
            throw invalid("ไม่พบข้อมูลรีวิว");
        }
        requireRating(request.overallRating(), "overallRating");
        requireRating(request.difficultyRating(), "difficultyRating");
        requireRating(request.workloadRating(), "workloadRating");

        String comment = request.comment() == null ? null : request.comment().trim();
        if (comment != null && comment.isEmpty()) {
            comment = null;
        }
        if (comment != null && comment.length() > MAX_COMMENT_LENGTH) {
            throw invalid("comment ยาวได้ไม่เกิน " + MAX_COMMENT_LENGTH + " ตัวอักษร");
        }
        return comment;
    }

    private static void requireRating(Integer value, String field) {
        if (value == null || value < 1 || value > 5) {
            throw invalid(field + " ต้องเป็นจำนวนเต็ม 1-5");
        }
    }

    private static ReviewException invalid(String message) {
        return new ReviewException(HttpStatus.BAD_REQUEST, ReviewException.INVALID_REVIEW, message);
    }

    private Course findCourse(String courseCode) {
        return courseRepository.findByCourseCode(courseCode)
                .orElseThrow(() -> new CourseNotFoundException("Course with code '" + courseCode + "' not found"));
    }

    private User findUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public static ReviewDto toDto(Review review) {
        return new ReviewDto(
                review.getReviewId(),
                review.getUser() != null ? review.getUser().getDisplayName() : null,
                review.getOverallRating(),
                review.getDifficultyRating(),
                review.getWorkloadRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }

    private ReviewSummaryDto buildSummary(Integer courseId) {
        ReviewRepository.ReviewStatsView stats = reviewRepository.getStats(courseId, ReviewStatus.visible);
        long reviewCount = stats.getReviewCount() != null ? stats.getReviewCount() : 0;

        List<Long> ratingCounts = new ArrayList<>(Collections.nCopies(5, 0L));
        for (ReviewRepository.RatingCountView row : reviewRepository.countByRating(courseId, ReviewStatus.visible)) {
            Integer rating = row.getRating();
            if (rating != null && rating >= 1 && rating <= 5) {
                ratingCounts.set(rating - 1, row.getTotal());
            }
        }

        return new ReviewSummaryDto(
                reviewCount,
                round1(stats.getAverageRating()),
                ratingCounts,
                round1(stats.getAverageDifficulty()),
                round1(stats.getAverageWorkload())
        );
    }

    /** latest = ใหม่สุดก่อน, highest / lowest = ตามคะแนนรวม แล้วใหม่สุดก่อน */
    private static Sort toSort(String sort) {
        Sort newestFirst = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("reviewId"));
        return switch (sort == null ? "latest" : sort.toLowerCase()) {
            case "latest" -> newestFirst;
            case "highest" -> Sort.by(Sort.Order.desc("overallRating")).and(newestFirst);
            case "lowest" -> Sort.by(Sort.Order.asc("overallRating")).and(newestFirst);
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "sort ต้องเป็น latest, highest หรือ lowest");
        };
    }

    private static double round1(Double value) {
        return value == null ? 0.0 : Math.round(value * 10) / 10.0;
    }
}
