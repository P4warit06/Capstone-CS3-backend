package com.example.demo.repository;

import com.example.demo.entity.CurriculumCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CurriculumCategoryRepository extends JpaRepository<CurriculumCategory, Integer> {
    List<CurriculumCategory> findByCurriculumVersionCurriculumVersionId(Integer curriculumVersionId);
}
