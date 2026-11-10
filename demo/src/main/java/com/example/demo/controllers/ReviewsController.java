package com.example.demo.controllers;

import com.example.demo.dtos.CourseReviewsResponseDto;
import com.example.demo.dtos.CreateReviewRequestDto;
import com.example.demo.dtos.MyReviewStatusDto;
import com.example.demo.dtos.ReviewDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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

    // SCRUM-203: ต้อง login — เขียนรีวิวได้เฉพาะวิชาที่เคยเรียน, 1 คน 1 รีวิวต่อวิชา
    @PostMapping
    public ResponseEntity<ReviewDto> createReview(
            @AuthenticationPrincipal Jwt token,
            @PathVariable String courseCode,
            @RequestBody CreateReviewRequestDto request) {
        ReviewDto created = reviewsService.createReview(token.getSubject(), courseCode, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // SCRUM-203: ต้อง login — FE ใช้ตัดสินใจว่าจะแสดงปุ่ม "เขียนรีวิว" ไหม
    @GetMapping("/me")
    public MyReviewStatusDto getMyReviewStatus(
            @AuthenticationPrincipal Jwt token,
            @PathVariable String courseCode) {
        return reviewsService.getMyReviewStatus(token.getSubject(), courseCode);
    }
}
