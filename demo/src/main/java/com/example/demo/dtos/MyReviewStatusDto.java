package com.example.demo.dtos;

/**
 * GET /v1/courses/{courseCode}/reviews/me
 * reason: null (รีวิวได้) | NOT_ENROLLED | ALREADY_REVIEWED
 * myReview: รีวิวของตัวเอง (ถ้าเคยเขียน) ไม่งั้น null
 */
public record MyReviewStatusDto(
        boolean canReview,
        String reason,
        ReviewDto myReview
) {}
