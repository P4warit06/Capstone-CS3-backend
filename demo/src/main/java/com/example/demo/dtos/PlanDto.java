package com.example.demo.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanDto {
    private Integer studyPlanId;
    private String planName;
    private Boolean isDefault;
    private PlanTrackDto track; // ถ้าไม่มี track ค่าจะเป็น null
    private LocalDateTime updatedAt;
}
