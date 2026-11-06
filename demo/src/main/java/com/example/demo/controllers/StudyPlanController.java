package com.example.demo.controllers;

import com.example.demo.dtos.AddPlanCourseRequestDto;
import com.example.demo.dtos.CreatePlanRequestDto;
import com.example.demo.dtos.PlanDto;
import com.example.demo.dtos.PlanListResponseDto;
import com.example.demo.dtos.StudyPlanDetailDto;
import com.example.demo.services.planners.StudyPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/plans")
@RequiredArgsConstructor
public class StudyPlanController {
    private final StudyPlanService studyPlanService;

    @GetMapping
    public ResponseEntity<PlanListResponseDto> getMyPlans(@AuthenticationPrincipal Jwt token) {
        PlanListResponseDto response = studyPlanService.getUserPlansByEmail(token.getSubject());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudyPlanDetailDto> getPlan(
            @AuthenticationPrincipal Jwt token,
            @PathVariable Integer id) {
        return ResponseEntity.ok(studyPlanService.getPlanById(token.getSubject(), id));
    }

    @PostMapping
    public ResponseEntity<PlanDto> createPlan(
            @AuthenticationPrincipal Jwt token,
            @RequestBody CreatePlanRequestDto request) {
        PlanDto response = studyPlanService.createPlanByEmail(token.getSubject(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // SCRUM-171: เพิ่มวิชาลงแผน (วิชาที่นักศึกษาเลือกเอง)
    @PostMapping("/{id}/courses")
    public ResponseEntity<StudyPlanDetailDto.Course> addCourse(
            @AuthenticationPrincipal Jwt token,
            @PathVariable Integer id,
            @RequestBody AddPlanCourseRequestDto request) {
        StudyPlanDetailDto.Course response = studyPlanService.addCourseToPlan(token.getSubject(), id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
