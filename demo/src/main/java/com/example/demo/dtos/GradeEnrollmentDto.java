package com.example.demo.dtos;

public interface GradeEnrollmentDto {
    Integer getEnrollmentId();
    Integer getCourseId();
    String getCourseCode();
    String getNameTh();
    String getNameEn();
    Integer getAcademicYear();
    Integer getSemester();
    String getEnrollmentStatus();
    String getCurrentGrade();
}
