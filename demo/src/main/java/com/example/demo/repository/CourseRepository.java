package com.example.demo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import com.example.demo.entity.Course;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findAll();
    Course save(Course course);
    void deleteById(Long id);
    Optional<Course> findByCourseId(Integer courseId);
    Optional<Course> findByCourseCode(String courseCode);

    // SCRUM-171: ค้นหาวิชาจากรหัส / ชื่อไทย / ชื่ออังกฤษ (ไม่สนตัวพิมพ์เล็กใหญ่)
    @Query("""
            SELECT c FROM Course c
            WHERE LOWER(c.courseCode) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(c.nameTh) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(c.nameEn) LIKE LOWER(CONCAT('%', :q, '%'))
            """)
    Page<Course> search(@Param("q") String q, Pageable pageable);

    // SCRUM-218: ค้นหา + filter ตามหมวด (categories.code) — ได้วิชาในหมวดย่อยของหมวดที่เลือกด้วย
    // q ว่าง ("") = ไม่กรองด้วยคำค้น, codes ต้องไม่ว่าง (ถ้าไม่ filter หมวดให้ใช้ findAll / search)
    @Query("""
            SELECT c FROM Course c
            WHERE (LOWER(c.courseCode) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(c.nameTh) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(c.nameEn) LIKE LOWER(CONCAT('%', :q, '%')))
              AND EXISTS (
                SELECT 1 FROM CurriculumCategoryCourse ccc
                JOIN ccc.curriculumCategory cc
                JOIN cc.category cat
                LEFT JOIN cc.parentCurriculumCategory pcc
                LEFT JOIN pcc.category pcat
                WHERE ccc.course = c
                  AND (cat.code IN :codes OR pcat.code IN :codes)
              )
            """)
    Page<Course> searchByCategoryCodes(@Param("q") String q,
                                       @Param("codes") List<String> codes,
                                       Pageable pageable);
}
