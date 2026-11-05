package com.example.demo.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyPlanDetailDto {
    private Integer studyPlanId;
    private String planName;
    private Boolean isDefault;
    private Student student;
    private Curriculum curriculum;
    private PlanTrackDto track;
    private List<Year> years;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Student {
        private String studentId;
        private Integer admissionYear;
        private Integer currentYear;
        private Integer currentSemester;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Curriculum {
        private Integer id;
        private String name;
        private Integer totalCredits;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Year {
        private Integer year;
        private Integer academicYear;
        private String status;
        private Integer totalCredits;
        private List<Semester> semesters;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Semester {
        private Integer semester;
        private String status;
        private Integer totalCredits;
        private Integer itFoundationCredits;
        private Integer generalEducationCredits;
        private Integer trackCredits;
        private Integer projectAndWorkCredits;
        private Integer freeElectiveCredits;
        private List<Course> courses;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Course {
        private Integer courseId;
        private String courseCode;
        private String nameTh;
        private String nameEn;
        private Integer credits;
        private String source;
        private Boolean locked;
        private CourseCategory category;
        private String status;
        private Integer studyPlanCourseId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseCategory {
        private String code;
        private String label;
    }
}
