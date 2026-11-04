package com.example.demo.controllers;

import com.example.demo.dtos.PlanListResponse;
import com.example.demo.services.planners.StudyPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/plans")
@RequiredArgsConstructor
public class StudyPlanController {
    private final StudyPlanService studyPlanService;

    @GetMapping
    public ResponseEntity<PlanListResponse> getMyPlans(@AuthenticationPrincipal Jwt token) {
        PlanListResponse response = studyPlanService.getUserPlansByEmail(token.getSubject());
        return ResponseEntity.ok(response);
    }
}
