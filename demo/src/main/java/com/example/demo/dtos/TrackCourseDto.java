package com.example.demo.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrackCourseDto {
    private Integer id;
    private String courseCode;
    private String nameTh;
    private String nameEn;
    private Integer credits;
    private Boolean isRequired;
    private Double rating;
    private Integer choiceGroup;

    public TrackCourseDto() {}

    public TrackCourseDto(Integer id, String courseCode, String nameTh, String nameEn, Integer credits, Boolean isRequired, Double rating, Integer choiceGroup) {
        this.id = id;
        this.courseCode = courseCode;
        this.nameTh = nameTh;
        this.nameEn = nameEn;
        this.credits = credits;
        this.isRequired = isRequired;
        this.rating = rating;
        this.choiceGroup = choiceGroup;
    }
}
