package com.example.demo.dtos;

import lombok.Data;

@Data
public class CreatePlanRequestDto {
    private String planName;
    private Integer trackId;
}
