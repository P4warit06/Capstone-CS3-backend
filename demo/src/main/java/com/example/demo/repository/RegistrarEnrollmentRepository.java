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

    /**
     * SCRUM-203: ลงทะเบียนวิชานี้จริง (ไม่ใช่ W) ในเทอมที่จบแล้ว — ก่อน (currentAcademicYear, currentSemester)
     */
    @Query("""
            SELECT COUNT(e) > 0 FROM RegistrarEnrollment e
            WHERE e.studentId = :studentId
              AND e.courseId = :courseId
              AND e.status = 'registered'
              AND (e.academicYear < :academicYear
                   OR (e.academicYear = :academicYear AND e.semester < :semester))
            """)
    boolean existsCompletedEnrollment(@Param("studentId") String studentId,
                                      @Param("courseId") Integer courseId,
                                      @Param("academicYear") Integer academicYear,
                                      @Param("semester") Integer semester);

    /** เคยลงวิชานี้แล้วหรือยัง (ทุกสถานะ รวม W) */
    boolean existsByStudentIdAndCourseId(String studentId, Integer courseId);
}
