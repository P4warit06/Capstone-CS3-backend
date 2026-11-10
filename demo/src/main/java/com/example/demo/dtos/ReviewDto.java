package com.example.demo.dtos;

import java.time.LocalDateTime;

/** รีวิว 1 รายการ — ห้ามใส่ email / ชื่อจริง / studentId */
public record ReviewDto(
        Integer reviewId,
        String displayName,
        Integer overallRating,
        Integer difficultyRating,
        Integer workloadRating,
        String comment,
        LocalDateTime createdAt
) {}
