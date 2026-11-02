package com.example.demo.dtos;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class SemesterDto {
    private Integer year;
    private Integer semester;
    private Integer totalCredits;
    private List<TrackCourseDto> courses;

    public SemesterDto() {}

    public SemesterDto(Integer year, Integer semester, Integer totalCredits, List<TrackCourseDto> courses) {
        this.year = year;
        this.semester = semester;
        this.totalCredits = totalCredits;
        this.courses = courses;
    }
}
