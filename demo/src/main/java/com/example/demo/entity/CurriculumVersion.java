package com.example.demo.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "curriculum_versions")
public class CurriculumVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "curriculum_version_id")
    private Integer curriculumVersionId;

    @Column(name = "version_name", nullable = false, length = 100)
    private String versionName;

    @Column(name = "academic_year", nullable = false, unique = true)
    private Integer academicYear;

    @Column(name = "total_credits", nullable = false)
    private Integer totalCredits;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public CurriculumVersion() {}
}