package com.example.demo.dtos;

import lombok.Data;

@Data
public class AddPlanCourseRequestDto {
    private Integer courseId;
    private Integer studyYear;
    private Integer studySemester;
}
