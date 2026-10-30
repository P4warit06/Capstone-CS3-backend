package com.example.demo.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseDetailDto {
    private Long id;
    private String courseCode;
    private String nameTh;
    private String nameEn;
    private Integer credits;
    private String descriptionTh;
    private String descriptionEn;
    private Boolean isActive;
    private List<String> tags;
    private List<CategoryDto> categories;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
