package com.example.demo.services.planners;

import com.example.demo.repository.CoursePrerequisiteRepository;
import com.example.demo.repository.CoursePrerequisiteRepository.CoursePrerequisiteView;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
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

        PrerequisiteValidationService.ValidationResult result = service.validate(plan, Map.of())
                .get(new PrerequisiteValidationService.PlacementKey(400, 2, 1));

        assertFalse(result.isEligible());
        assertEquals(List.of(3), result.missingGroups().stream()
                .map(group -> group.getGroupNo()).toList());
        assertEquals(List.of(104), result.missingGroups().getFirst().getCourses().stream()
                .map(course -> course.getCourseId()).toList());
    }

    @Test
    void treatsRegisteredPrerequisiteAsSatisfiedWhenItWasRegisteredBeforeTheTargetTerm() {
        PrerequisiteValidationService service = serviceReturning(List.of(
                prerequisite(400, 101, 1, false)));

        PrerequisiteValidationService.ValidationResult result = service.validate(
                        List.of(planned(400, 2, 1)), registeredCourses(101))
                .get(new PrerequisiteValidationService.PlacementKey(400, 2, 1));

        assertTrue(result.isEligible());
        assertTrue(result.missingGroups().isEmpty());
    }

    @Test
    void registeredPrerequisiteWithoutAGradeIsSatisfied() {
        assertRegisteredPrerequisiteIsSatisfiedRegardlessOfGrade();
    }

    @Test
    void registeredPrerequisiteWithFailingGradeIsStillSatisfied() {
        assertRegisteredPrerequisiteIsSatisfiedRegardlessOfGrade();
    }

    @Test
    void withdrawnPrerequisiteIsNotSatisfied() {
        PrerequisiteValidationService service = serviceReturning(List.of(
                prerequisite(400, 101, 1, false)));

        PrerequisiteValidationService.ValidationResult result = service.validate(
                        List.of(planned(400, 2, 1)), Map.of())
                .get(new PrerequisiteValidationService.PlacementKey(400, 2, 1));

        assertFalse(result.isEligible());
        assertEquals(List.of(1), result.missingGroups().stream().map(group -> group.getGroupNo()).toList());
    }

    @Test
    void missingPrerequisiteWithoutRegistrationOrPlanIsReported() {
        PrerequisiteValidationService service = serviceReturning(List.of(
                prerequisite(400, 101, 1, false)));

        PrerequisiteValidationService.ValidationResult result = service.validate(
                        List.of(planned(400, 2, 1)), Map.of())
                .get(new PrerequisiteValidationService.PlacementKey(400, 2, 1));

        assertFalse(result.isEligible());
    }

    @Test
    void doesNotAllowNonCorequisiteInTheSameTerm() {
        PrerequisiteValidationService service = serviceReturning(List.of(
                prerequisite(400, 101, 1, false)));

        PrerequisiteValidationService.ValidationResult result = service.validate(
                        List.of(planned(400, 1, 1), planned(101, 1, 1)), Map.of())
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
                        101, 1, 1, "registered")), Map.of())
                .get(new PrerequisiteValidationService.PlacementKey(400, 1, 1));

        assertTrue(result.isEligible());
        assertTrue(result.missingGroups().isEmpty());
    }

    private void assertRegisteredPrerequisiteIsSatisfiedRegardlessOfGrade() {
        PrerequisiteValidationService service = serviceReturning(List.of(
                prerequisite(400, 101, 1, false)));

        PrerequisiteValidationService.ValidationResult result = service.validate(
                        List.of(planned(400, 2, 1)), registeredCourses(101))
                .get(new PrerequisiteValidationService.PlacementKey(400, 2, 1));

        assertTrue(result.isEligible());
    }

    private static Map<PrerequisiteValidationService.PlacementKey, Set<Integer>> registeredCourses(
            int... registeredCourseIds) {
        return Map.of(new PrerequisiteValidationService.PlacementKey(400, 2, 1),
                java.util.Arrays.stream(registeredCourseIds).boxed().collect(java.util.stream.Collectors.toSet()));
    }

    private PrerequisiteValidationService serviceReturning(List<CoursePrerequisiteView> prerequisites) {
        CoursePrerequisiteRepository repository = courseIds -> prerequisites;
        return new PrerequisiteValidationService(repository);
    }

    private static PrerequisiteValidationService.PlannedCourse planned(int courseId, int year, int semester) {
        return new PrerequisiteValidationService.PlannedCourse(courseId, year, semester, "planned");
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
