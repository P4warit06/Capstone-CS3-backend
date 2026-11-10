package com.example.demo.controllers;

import com.example.demo.dtos.CourseDto;
import com.example.demo.services.courses.SavedCourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/v1/saved-courses")
@RequiredArgsConstructor
public class SavedCourseController {

    private final SavedCourseService savedCourseService;

    @GetMapping
    public ResponseEntity<List<CourseDto>> getSavedCourses(@AuthenticationPrincipal Jwt token) {
        return ResponseEntity.ok(savedCourseService.getSavedCourses(getUserId(token)));
    }

    @PostMapping("/{courseId}")
    public ResponseEntity<CourseDto> saveCourse(
            @AuthenticationPrincipal Jwt token,
            @PathVariable Integer courseId) {
        CourseDto response = savedCourseService.saveCourse(getUserId(token), courseId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<Void> removeCourse(
            @AuthenticationPrincipal Jwt token,
            @PathVariable Integer courseId) {
        savedCourseService.removeCourse(getUserId(token), courseId);
        return ResponseEntity.noContent().build();
    }

    private Integer getUserId(Jwt token) {
        Number userId = token.getClaim("userId");
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "ไม่พบ userId ในโทเคน");
        }
        return userId.intValue();
    }
}
