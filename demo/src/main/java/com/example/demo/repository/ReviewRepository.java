package com.example.demo.repository;

import com.example.demo.entity.Review;
import com.example.demo.entity.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    // นับเฉพาะรีวิวที่แสดงอยู่ (ใช้ในหน้า Track)
    @Query(value = """
            SELECT AVG(overall_rating)
            FROM reviews
            WHERE course_id = :courseId
              AND status = CAST('visible' AS review_status)
            """, nativeQuery = true)
    Double getAverageRatingByCourseId(@Param("courseId") Integer courseId);

    // SCRUM-206: รายการรีวิวของวิชา (ดึง user มาพร้อมกันเพื่อเอา displayName)
    @EntityGraph(attributePaths = "user")
    Page<Review> findByCourse_CourseIdAndStatus(Integer courseId, ReviewStatus status, Pageable pageable);

    // SCRUM-203: 1 คน 1 รีวิวต่อวิชา (นับรวมรีวิวที่ถูกซ่อน)
    boolean existsByUserIdAndCourse_CourseId(Integer userId, Integer courseId);

    @EntityGraph(attributePaths = "user")
    Optional<Review> findByUserIdAndCourse_CourseId(Integer userId, Integer courseId);

    @Query("""
            SELECT COUNT(r) AS reviewCount,
                   AVG(r.overallRating) AS averageRating,
                   AVG(r.difficultyRating) AS averageDifficulty,
                   AVG(r.workloadRating) AS averageWorkload
            FROM Review r
            WHERE r.course.courseId = :courseId AND r.status = :status
            """)
    ReviewStatsView getStats(@Param("courseId") Integer courseId, @Param("status") ReviewStatus status);

    @Query("""
            SELECT r.overallRating AS rating, COUNT(r) AS total
            FROM Review r
            WHERE r.course.courseId = :courseId AND r.status = :status
            GROUP BY r.overallRating
            """)
    List<RatingCountView> countByRating(@Param("courseId") Integer courseId, @Param("status") ReviewStatus status);

    interface ReviewStatsView {
        Long getReviewCount();
        Double getAverageRating();
        Double getAverageDifficulty();
        Double getAverageWorkload();
    }

    interface RatingCountView {
        Integer getRating();
        Long getTotal();
    }
}
