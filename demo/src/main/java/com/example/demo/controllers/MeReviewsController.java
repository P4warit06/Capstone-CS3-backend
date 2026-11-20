package com.example.demo.controllers;

import com.example.demo.dtos.MyReviewDto;
import com.example.demo.dtos.ReviewableCourseDto;
import com.example.demo.services.reviews.ReviewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** SCRUM-244: ข้อมูลรีวิวของผู้ใช้ที่ login อยู่ (ดึงจาก token เท่านั้น ไม่รับ user id จาก query) */
@RestController
@RequestMapping("/v1/me")
@RequiredArgsConstructor
public class MeReviewsController {

    private final ReviewsService reviewsService;

    @GetMapping("/reviewable-courses")
    public List<ReviewableCourseDto> getReviewableCourses(@AuthenticationPrincipal Jwt token) {
        return reviewsService.getReviewableCourses(token.getSubject());
    }

    @GetMapping("/reviews")
    public List<MyReviewDto> getMyReviews(@AuthenticationPrincipal Jwt token) {
        return reviewsService.getMyReviews(token.getSubject());
    }
}
