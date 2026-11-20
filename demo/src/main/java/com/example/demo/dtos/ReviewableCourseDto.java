package com.example.demo.dtos;

/**
 * SCRUM-244: วิชาที่รีวิวได้ (GET /v1/me/reviewable-courses)
 * studyYear / semester = เทอมล่าสุดที่ลงทะเบียนวิชานี้ (ลงซ้ำแสดงครั้งเดียว)
 */
public record ReviewableCourseDto(
        Integer courseId,
        String courseCode,
        String nameTh,
        String nameEn,
        Integer credits,
        Integer academicYear,
        Integer studyYear,
        Integer semester
) {}
