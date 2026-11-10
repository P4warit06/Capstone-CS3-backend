package com.example.demo.services.reviews;

import com.example.demo.dtos.CourseReviewsResponseDto;
import com.example.demo.dtos.PagedResponseDto;
import com.example.demo.dtos.ReviewDto;
import com.example.demo.dtos.ReviewSummaryDto;
import com.example.demo.entity.Course;
import com.example.demo.entity.Review;
import com.example.demo.entity.ReviewStatus;
import com.example.demo.exceptions.CourseNotFoundException;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.ReviewRepository;
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

    private final ReviewRepository reviewRepository;
    private final CourseRepository courseRepository;

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

        Course course = courseRepository.findByCourseCode(courseCode)
                .orElseThrow(() -> new CourseNotFoundException("Course with code '" + courseCode + "' not found"));
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
