package com.example.demo.services.planners;

import com.example.demo.dtos.StudyPlanDetailDto;
import com.example.demo.entity.RegistrarEnrollment;
import com.example.demo.entity.StudentCourseGrade;
import com.example.demo.repository.CoursePrerequisiteRepository;
import com.example.demo.repository.CoursePrerequisiteRepository.CoursePrerequisiteView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrerequisiteValidationService {

    private static final Set<String> PASSING_GRADES = Set.of("A", "B+", "B", "C+", "C", "D+", "D", "S");

    private final CoursePrerequisiteRepository coursePrerequisiteRepository;

    public Set<Integer> findPassedCourseIds(List<RegistrarEnrollment> enrollments,
                                            List<StudentCourseGrade> grades) {
        if (enrollments.isEmpty()) {
            return Set.of();
        }

        Map<Integer, StudentCourseGrade> latestGradesByEnrollment = new HashMap<>();
        Comparator<StudentCourseGrade> gradeOrder = Comparator
                .comparing(StudentCourseGrade::getUpdatedAt,
                        Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(StudentCourseGrade::getGradeId,
                        Comparator.nullsFirst(Comparator.naturalOrder()));
        for (StudentCourseGrade grade : grades) {
            latestGradesByEnrollment.merge(grade.getEnrollmentId(), grade,
                    (current, candidate) -> gradeOrder.compare(current, candidate) < 0 ? candidate : current);
        }

        Map<Integer, RegistrarEnrollment> latestEnrollmentByCourse = new HashMap<>();
        Comparator<RegistrarEnrollment> enrollmentOrder = Comparator
                .comparing(RegistrarEnrollment::getAcademicYear)
                .thenComparing(RegistrarEnrollment::getSemester)
                .thenComparing(RegistrarEnrollment::getEnrollmentId);
        for (RegistrarEnrollment enrollment : enrollments) {
            latestEnrollmentByCourse.merge(enrollment.getCourseId(), enrollment,
                    (current, candidate) -> enrollmentOrder.compare(current, candidate) < 0 ? candidate : current);
        }

        Set<Integer> passedCourseIds = new HashSet<>();
        for (RegistrarEnrollment enrollment : latestEnrollmentByCourse.values()) {
            StudentCourseGrade grade = latestGradesByEnrollment.get(enrollment.getEnrollmentId());
            if ("registered".equalsIgnoreCase(enrollment.getStatus())
                    && grade != null && isPassingGrade(grade.getGrade())) {
                passedCourseIds.add(enrollment.getCourseId());
            }
        }
        return passedCourseIds;
    }

    public Map<PlacementKey, ValidationResult> validate(
            Collection<PlannedCourse> plannedCourses, Set<Integer> passedCourseIds) {
        List<Integer> courseIds = plannedCourses.stream()
                .map(PlannedCourse::courseId)
                .distinct()
                .toList();
        if (courseIds.isEmpty()) {
            return Map.of();
        }

        Map<Integer, List<CoursePrerequisiteView>> prerequisitesByCourse = coursePrerequisiteRepository
                .findByCourseIds(courseIds).stream()
                .collect(Collectors.groupingBy(CoursePrerequisiteView::getCourseId));

        Map<PlacementKey, ValidationResult> results = new HashMap<>();
        for (PlannedCourse target : plannedCourses) {
            Map<Integer, List<CoursePrerequisiteView>> groups = prerequisitesByCourse
                    .getOrDefault(target.courseId(), List.of()).stream()
                    .collect(Collectors.groupingBy(CoursePrerequisiteView::getGroupNo));

            List<StudyPlanDetailDto.MissingPrerequisiteGroup> missingGroups = groups.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .filter(group -> group.getValue().stream().noneMatch(prerequisite ->
                            passedCourseIds.contains(prerequisite.getPrerequisiteCourseId())
                                    || isPlannedInTime(prerequisite, target, plannedCourses)))
                    .map(group -> StudyPlanDetailDto.MissingPrerequisiteGroup.builder()
                            .groupNo(group.getKey())
                            .courses(group.getValue().stream()
                                    .sorted(Comparator.comparing(CoursePrerequisiteView::getPrerequisiteCourseCode))
                                    .map(this::toPrerequisiteCourse)
                                    .toList())
                            .build())
                    .toList();

            results.put(new PlacementKey(target.courseId(), target.studyYear(), target.studySemester()),
                    new ValidationResult(missingGroups.isEmpty(), missingGroups));
        }
        return results;
    }

    private boolean isPlannedInTime(CoursePrerequisiteView prerequisite, PlannedCourse target,
                                    Collection<PlannedCourse> plannedCourses) {
        return plannedCourses.stream()
                .filter(course -> course.courseId().equals(prerequisite.getPrerequisiteCourseId()))
                .anyMatch(course -> {
                    int termComparison = compareTerms(course.studyYear(), course.studySemester(),
                            target.studyYear(), target.studySemester());
                    boolean isPlanned = "planned".equalsIgnoreCase(course.status());
                    if (Boolean.TRUE.equals(prerequisite.getCorequisite())) {
                        return (termComparison < 0 && isPlanned)
                                || (termComparison == 0
                                && (isPlanned || "registered".equalsIgnoreCase(course.status())));
                    }
                    return termComparison < 0 && isPlanned;
                });
    }

    private int compareTerms(int firstYear, int firstSemester, int secondYear, int secondSemester) {
        int yearComparison = Integer.compare(firstYear, secondYear);
        return yearComparison != 0 ? yearComparison : Integer.compare(firstSemester, secondSemester);
    }

    private boolean isPassingGrade(String grade) {
        if (grade == null) {
            return false;
        }
        String normalizedGrade = grade.trim().toUpperCase(java.util.Locale.ROOT);
        return PASSING_GRADES.contains(normalizedGrade);
    }

    private StudyPlanDetailDto.PrerequisiteCourse toPrerequisiteCourse(CoursePrerequisiteView prerequisite) {
        return StudyPlanDetailDto.PrerequisiteCourse.builder()
                .courseId(prerequisite.getPrerequisiteCourseId())
                .courseCode(prerequisite.getPrerequisiteCourseCode())
                .nameTh(prerequisite.getPrerequisiteCourseNameTh())
                .nameEn(prerequisite.getPrerequisiteCourseNameEn())
                .isCorequisite(prerequisite.getCorequisite())
                .build();
    }

    public record PlannedCourse(Integer courseId, int studyYear, int studySemester, String status) {
    }

    public record PlacementKey(Integer courseId, Integer studyYear, Integer studySemester) {
    }

    public record ValidationResult(boolean isEligible,
                                   List<StudyPlanDetailDto.MissingPrerequisiteGroup> missingGroups) {
    }
}
