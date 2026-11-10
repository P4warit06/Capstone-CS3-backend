package com.example.demo.dtos;

public record CourseReviewsResponseDto(
        ReviewSummaryDto summary,
        PagedResponseDto<ReviewDto> reviews
) {}
