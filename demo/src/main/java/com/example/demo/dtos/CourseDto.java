package com.example.demo.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class CourseDto {
    private Long id;
    private String courseCode;
    private String nameTh;
    private String nameEn;
    private Integer credits;
    private Boolean isActive;
    private List<String> tags;
    private List<CategoryDto> categories;

    public CourseDto() {}

    public CourseDto(Long id, String courseCode, String nameTh, String nameEn, Integer credits, Boolean isActive, List<String> tags, List<CategoryDto> categories) {
        this.id = id;
        this.courseCode = courseCode;
        this.nameTh = nameTh;
        this.nameEn = nameEn;
        this.credits = credits;
        this.isActive = isActive;
        this.tags = tags;
        this.categories = categories;
    }
}
