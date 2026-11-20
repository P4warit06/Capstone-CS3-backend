package com.example.demo.dtos;

import java.time.LocalDateTime;

/** SCRUM-244: รีวิวของตัวเอง (GET /v1/me/reviews) — status: visible | hidden */
public record MyReviewDto(
        Integer reviewId,
        String courseCode,
        String nameTh,
        String nameEn,
        Integer overallRating,
        Integer difficultyRating,
        Integer workloadRating,
        String comment,
        String status,
        LocalDateTime createdAt
) {}
