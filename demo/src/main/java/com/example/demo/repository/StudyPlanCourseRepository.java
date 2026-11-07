package com.example.demo.repository;

import com.example.demo.entity.StudyPlanCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudyPlanCourseRepository extends JpaRepository<StudyPlanCourse, Integer> {
    List<StudyPlanCourse> findByStudyPlanStudyPlanId(Integer studyPlanId);

    Optional<StudyPlanCourse> findByStudyPlanCourseIdAndStudyPlanStudyPlanId(
            Integer studyPlanCourseId, Integer studyPlanId);

    boolean existsByStudyPlanStudyPlanIdAndCourseCourseIdAndStudyYearAndStudySemester(
            Integer studyPlanId, Integer courseId, Integer studyYear, Integer studySemester);
}
