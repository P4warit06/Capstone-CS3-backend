package com.example.demo.dtos;

import java.time.LocalDateTime;

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
