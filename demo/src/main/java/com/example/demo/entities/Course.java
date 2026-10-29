package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id") // ตรงกับ PK ใน SQL
    private Integer courseId;

    @Column(name = "course_code", nullable = false, unique = true, length = 20)
    private String courseCode;

    @Column(name = "name_th", nullable = false)
    private String nameTh;

    @Column(name = "name_en", nullable = false)
    private String nameEn;

    @Column(nullable = false)
    private Integer credits;

    @Column(name = "description_th", columnDefinition = "text")
    private String descriptionTh;

    @Column(name = "description_en", columnDefinition = "text")
    private String descriptionEn;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Course() {}
}