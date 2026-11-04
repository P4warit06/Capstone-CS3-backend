package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "registrar_students")
public class RegistrarStudent {
    @Id
    @Column(name = "student_id", nullable = false, length = 20)
    private String studentId;

    @Column(name = "first_name_th", nullable = false, length = 100)
    private String firstNameTh;

    @Column(name = "last_name_th", nullable = false, length = 100)
    private String lastNameTh;

    @Column(name = "admission_year", nullable = false)
    private Integer admissionYear;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "curriculum_version_id", nullable = false)
    private CurriculumVersion curriculumVersion;

    @Column(name = "current_year", nullable = false)
    private Integer currentYear;

    @Column(name = "current_semester", nullable = false)
    private Integer currentSemester;


}