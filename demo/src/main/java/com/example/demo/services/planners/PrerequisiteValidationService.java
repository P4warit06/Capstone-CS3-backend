package com.example.demo.services.planners;

import com.example.demo.dtos.StudyPlanDetailDto;
import com.example.demo.repository.CoursePrerequisiteRepository;
import com.example.demo.repository.CoursePrerequisiteRepository.CoursePrerequisiteView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrerequisiteValidationService {

    private final CoursePrerequisiteRepository coursePrerequisiteRepository;

    public Map<PlacementKey, ValidationResult> validate(
            Collection<PlannedCourse> plannedCourses,
            Map<PlacementKey, Set<Integer>> registeredCourseIdsByPlacement) {
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
                            registeredCourseIdsByPlacement.getOrDefault(
                                            new PlacementKey(target.courseId(), target.studyYear(), target.studySemester()),
                                            Set.of())
                                    .contains(prerequisite.getPrerequisiteCourseId())
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
