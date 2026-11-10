package com.example.demo.controllers;

import com.example.demo.dtos.CourseReviewsResponseDto;
import com.example.demo.services.reviews.ReviewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/courses/{courseCode}/reviews")
@RequiredArgsConstructor
public class ReviewsController {

    private final ReviewsService reviewsService;

    // SCRUM-206: public — Guest อ่านได้
    @GetMapping
    public CourseReviewsResponseDto getCourseReviews(
            @PathVariable String courseCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "latest") String sort) {
        return reviewsService.getCourseReviews(courseCode, page, size, sort);
    }
}
