package com.example.demo.dtos;

/** POST /v1/courses/{courseCode}/reviews — rating ทั้ง 3 ตัวบังคับ 1-5, comment ไม่บังคับ */
public record CreateReviewRequestDto(
        Integer overallRating,
        Integer difficultyRating,
        Integer workloadRating,
        String comment
) {}
