package com.example.demo.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GradeRequestDto {
    private Integer enrollmentId;
    private String grade;
}
