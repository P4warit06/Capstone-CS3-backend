package com.example.demo.controllers;

import com.example.demo.dtos.GradeDto;
import com.example.demo.dtos.GradeEnrollmentDto;
import com.example.demo.dtos.GradeRequestDto;
import com.example.demo.services.grades.GradesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/grades")
@RequiredArgsConstructor
public class GradeController {
    private final GradesService gradeService;

    @GetMapping
    public ResponseEntity<List<GradeDto>> getGrades(@AuthenticationPrincipal Jwt token) {
        return ResponseEntity.ok(gradeService.getGrades(token.getSubject()));
    }

    @GetMapping("/enrollments")
    public ResponseEntity<List<GradeEnrollmentDto>> getRegisteredEnrollments(
            @AuthenticationPrincipal Jwt token) {
        return ResponseEntity.ok(gradeService.getRegisteredEnrollments(token.getSubject()));
    }

    @PutMapping
    public ResponseEntity<GradeDto> updateGrade(
            @AuthenticationPrincipal Jwt token,
            @RequestBody GradeRequestDto request) {
        return ResponseEntity.ok(gradeService.updateGrade(token.getSubject(), request));
    }
}
