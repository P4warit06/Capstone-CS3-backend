package com.example.demo.repository;

import com.example.demo.entity.CurriculumCategoryCourse;
import com.example.demo.entity.CurriculumCategoryCourse.CurriculumCategoryCourseId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CurriculumCategoryCourseRepository
        extends JpaRepository<CurriculumCategoryCourse, CurriculumCategoryCourseId> {
    List<CurriculumCategoryCourse> findByCurriculumCategoryCurriculumVersionCurriculumVersionId(
            Integer curriculumVersionId);
}
