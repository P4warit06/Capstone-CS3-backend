package com.example.demo.services.planners;

import com.example.demo.entity.RegistrarEnrollment;
import com.example.demo.entity.StudentCourseGrade;
import com.example.demo.repository.CoursePrerequisiteRepository;
import com.example.demo.repository.CoursePrerequisiteRepository.CoursePrerequisiteView;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PrerequisiteValidationServiceTest {

    @Test
    void appliesOrWithinGroupsAndAndAcrossGroups() {
        List<CoursePrerequisiteView> prerequisites = List.of(
                prerequisite(400, 101, 1, false),
                prerequisite(400, 102, 1, false),
                prerequisite(400, 103, 2, true),
                prerequisite(400, 104, 3, false));
        PrerequisiteValidationService service = serviceReturning(prerequisites);
        List<PrerequisiteValidationService.PlannedCourse> plan = List.of(
                planned(400, 2, 1),
                planned(101, 1, 2),
                planned(102, 2, 1),
                planned(103, 2, 1),
                planned(104, 2, 1));

        PrerequisiteValidationService.ValidationResult result = service.validate(plan, Set.of())
                .get(new PrerequisiteValidationService.PlacementKey(400, 2, 1));

        assertFalse(result.isEligible());
        assertEquals(List.of(3), result.missingGroups().stream()
                .map(group -> group.getGroupNo()).toList());
        assertEquals(List.of(104), result.missingGroups().getFirst().getCourses().stream()
                .map(course -> course.getCourseId()).toList());
    }

    @Test
    void treatsPreviouslyPassedPrerequisiteAsSatisfiedRegardlessOfPlannedTerm() {
        PrerequisiteValidationService service = serviceReturning(List.of(
                prerequisite(400, 101, 1, false)));

        PrerequisiteValidationService.ValidationResult result = service.validate(
                        List.of(planned(400, 1, 1)), Set.of(101))
                .get(new PrerequisiteValidationService.PlacementKey(400, 1, 1));

        assertTrue(result.isEligible());
        assertTrue(result.missingGroups().isEmpty());
    }

    @Test
    void doesNotAllowNonCorequisiteInTheSameTerm() {
        PrerequisiteValidationService service = serviceReturning(List.of(
                prerequisite(400, 101, 1, false)));

        PrerequisiteValidationService.ValidationResult result = service.validate(
                        List.of(planned(400, 1, 1), planned(101, 1, 1)), Set.of())
                .get(new PrerequisiteValidationService.PlacementKey(400, 1, 1));

        assertFalse(result.isEligible());
        assertEquals(List.of(1), result.missingGroups().stream()
                .map(group -> group.getGroupNo()).toList());
    }

    @Test
    void allowsRegisteredCorequisiteInTheSameTerm() {
        PrerequisiteValidationService service = serviceReturning(List.of(
                prerequisite(400, 101, 1, true)));

        PrerequisiteValidationService.ValidationResult result = service.validate(
                List.of(planned(400, 1, 1), new PrerequisiteValidationService.PlannedCourse(
                        101, 1, 1, "registered")), Set.of())
                .get(new PrerequisiteValidationService.PlacementKey(400, 1, 1));

        assertTrue(result.isEligible());
        assertTrue(result.missingGroups().isEmpty());
    }

    @Test
    void usesTheLatestEnrollmentAndLatestGradeForEachCourse() {
        PrerequisiteValidationService service = serviceReturning(List.of());
        List<RegistrarEnrollment> enrollments = List.of(
                enrollment(1, 10, 2567, 1, "registered"),
                enrollment(2, 10, 2568, 1, "registered"),
                enrollment(3, 11, 2568, 1, "registered"),
                enrollment(4, 12, 2568, 1, "withdrawn"),
                enrollment(5, 13, 2568, 1, "registered"),
                enrollment(6, 14, 2568, 1, "registered"));
        List<StudentCourseGrade> grades = List.of(
                grade(1, 1, "A", LocalDateTime.parse("2025-01-01T00:00:00")),
                grade(2, 2, "F", LocalDateTime.parse("2025-02-01T00:00:00")),
                grade(3, 3, "U", LocalDateTime.parse("2025-02-01T00:00:00")),
                grade(4, 4, "B", LocalDateTime.parse("2025-02-01T00:00:00")),
                grade(5, 5, "A", LocalDateTime.parse("2025-02-01T00:00:00")),
                grade(6, 5, "F", LocalDateTime.parse("2025-03-01T00:00:00")),
                grade(7, 6, "D", LocalDateTime.parse("2025-03-01T00:00:00")));

        Set<Integer> passedCourseIds = service.findPassedCourseIds(enrollments, grades);

        assertEquals(Set.of(14), passedCourseIds);
    }

    private PrerequisiteValidationService serviceReturning(List<CoursePrerequisiteView> prerequisites) {
        CoursePrerequisiteRepository repository = courseIds -> prerequisites;
        return new PrerequisiteValidationService(repository);
    }

    private static PrerequisiteValidationService.PlannedCourse planned(int courseId, int year, int semester) {
        return new PrerequisiteValidationService.PlannedCourse(courseId, year, semester, "planned");
    }

    private static RegistrarEnrollment enrollment(int enrollmentId, int courseId, int academicYear,
                                                  int semester, String status) {
        RegistrarEnrollment enrollment = new RegistrarEnrollment();
        enrollment.setEnrollmentId(enrollmentId);
        enrollment.setCourseId(courseId);
        enrollment.setAcademicYear(academicYear);
        enrollment.setSemester(semester);
        enrollment.setStatus(status);
        return enrollment;
    }

    private static StudentCourseGrade grade(int gradeId, int enrollmentId, String value, LocalDateTime updatedAt) {
        StudentCourseGrade grade = new StudentCourseGrade();
        grade.setGradeId(gradeId);
        grade.setEnrollmentId(enrollmentId);
        grade.setGrade(value);
        grade.setUpdatedAt(updatedAt);
        return grade;
    }

    private static CoursePrerequisiteView prerequisite(int courseId, int prerequisiteCourseId,
                                                       int groupNo, boolean corequisite) {
        return new CoursePrerequisiteView() {
            @Override
            public Integer getCourseId() {
                return courseId;
            }

            @Override
            public Integer getPrerequisiteCourseId() {
                return prerequisiteCourseId;
            }

            @Override
            public Integer getGroupNo() {
                return groupNo;
            }

            @Override
            public Boolean getCorequisite() {
                return corequisite;
            }

            @Override
            public String getPrerequisiteCourseCode() {
                return "COURSE-" + prerequisiteCourseId;
            }

            @Override
            public String getPrerequisiteCourseNameTh() {
                return "ชื่อวิชา " + prerequisiteCourseId;
            }

            @Override
            public String getPrerequisiteCourseNameEn() {
                return "Course " + prerequisiteCourseId;
            }
        };
    }
}
