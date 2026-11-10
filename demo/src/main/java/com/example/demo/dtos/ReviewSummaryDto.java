package com.example.demo.dtos;

import java.util.List;

/** ratingCounts: index 0 = 1 ดาว ... index 4 = 5 ดาว */
public record ReviewSummaryDto(
        long reviewCount,
        double averageRating,
        List<Long> ratingCounts,
        double averageDifficulty,
        double averageWorkload
) {}
