package com.example.demo.repository;

import com.example.demo.entity.Course;
import com.example.demo.entity.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findAll();
    Course save(Course course);
    void deleteById(Long id);
    Optional<Course> findByCourseId(Integer courseId);
    Optional<Course> findByCourseCode(String courseCode);

    @Query(value = """
            SELECT c FROM Course c
            WHERE (:hasQuery = false
                OR LOWER(c.courseCode) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(c.nameTh) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(c.nameEn) LIKE LOWER(CONCAT('%', :q, '%')))
              AND (:hasCodes = false OR EXISTS (
                SELECT 1 FROM CurriculumCategoryCourse ccc
                JOIN ccc.curriculumCategory cc
                JOIN cc.category cat
                LEFT JOIN cc.parentCurriculumCategory pcc
                LEFT JOIN pcc.category pcat
                WHERE ccc.course = c
                  AND (cat.code IN :codes OR pcat.code IN :codes)
              ))
            ORDER BY
              CASE WHEN :sort = 'rating,desc' AND (
                  SELECT COUNT(r0) FROM Review r0
                  WHERE r0.course = c AND r0.status = :visibleStatus
              ) = 0 THEN 1 ELSE 0 END ASC,
              CASE WHEN :sort = 'rating,desc' THEN (
                  SELECT AVG(r1.overallRating) FROM Review r1
                  WHERE r1.course = c AND r1.status = :visibleStatus
              ) END DESC,
              CASE WHEN :sort = 'reviewCount,desc' THEN (
                  SELECT COUNT(r2) FROM Review r2
                  WHERE r2.course = c AND r2.status = :visibleStatus
              ) END DESC,
              CASE WHEN :sort = 'courseCode,asc' THEN c.courseCode END ASC,
              CASE WHEN :sort = 'courseCode,desc' THEN c.courseCode END DESC,
              c.courseCode ASC
            """,
            countQuery = """
            SELECT COUNT(c) FROM Course c
            WHERE (:hasQuery = false
                OR LOWER(c.courseCode) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(c.nameTh) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(c.nameEn) LIKE LOWER(CONCAT('%', :q, '%')))
              AND (:hasCodes = false OR EXISTS (
                SELECT 1 FROM CurriculumCategoryCourse ccc
                JOIN ccc.curriculumCategory cc
                JOIN cc.category cat
                LEFT JOIN cc.parentCurriculumCategory pcc
                LEFT JOIN pcc.category pcat
                WHERE ccc.course = c
                  AND (cat.code IN :codes OR pcat.code IN :codes)
              ))
            """)
    Page<Course> findCourses(@Param("q") String q,
                             @Param("hasQuery") boolean hasQuery,
                             @Param("hasCodes") boolean hasCodes,
                             @Param("codes") List<String> codes,
                             @Param("sort") String sort,
                             @Param("visibleStatus") ReviewStatus visibleStatus,
                             Pageable pageable);

}
