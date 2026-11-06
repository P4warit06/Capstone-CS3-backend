package com.example.demo.repository;

import com.example.demo.entity.RegistrarEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RegistrarEnrollmentRepository extends JpaRepository<RegistrarEnrollment, Integer> {
    List<RegistrarEnrollment> findByStudentId(String studentId);

    /**
     * ลงทะเบียนวิชานี้แล้วและยังไม่ติด F/U (ผ่านแล้ว หรือกำลังเรียน/ยังไม่ออกเกรด)
     * W และ F/U ไม่นับ เพราะต้องวางแผนลงใหม่ได้
     */
    @Query(value = """
            SELECT EXISTS (
              SELECT 1
              FROM registrar_enrollments e
              LEFT JOIN student_course_grades g ON g.enrollment_id = e.enrollment_id
              WHERE e.student_id = :studentId
                AND e.course_id = :courseId
                AND e.status = 'registered'
                AND (g.grade IS NULL OR g.grade NOT IN ('F', 'U'))
            )
            """, nativeQuery = true)
    boolean existsActiveEnrollment(@Param("studentId") String studentId, @Param("courseId") Integer courseId);

    /** เคยลงวิชานี้แล้วหรือยัง (ทุกสถานะ รวม W) */
    boolean existsByStudentIdAndCourseId(String studentId, Integer courseId);
}
