package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Integer reviewId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    // อ่านอย่างเดียว ใช้ดึง displayName (เขียนผ่าน userId)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "overall_rating", nullable = false)
    private Integer overallRating;

    @Column(name = "difficulty_rating", nullable = false)
    private Integer difficultyRating;

    @Column(name = "workload_rating", nullable = false)
    private Integer workloadRating;

    @Column(columnDefinition = "text")
    private String comment;

    // Maps to the PostgreSQL enum type "review_status"
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "review_status")
    private ReviewStatus status = ReviewStatus.visible;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Review() {}
}
