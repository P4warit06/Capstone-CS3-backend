package com.example.demo.dtos;

import java.util.List;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getNameTh() {
        return nameTh;
    }

    public void setNameTh(String nameTh) {
        this.nameTh = nameTh;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public Integer getCredits() {
        return credits;
    }

    public void setCredits(Integer credits) {
        this.credits = credits;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<CategoryDto> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryDto> categories) {
        this.categories = categories;
    }
}
