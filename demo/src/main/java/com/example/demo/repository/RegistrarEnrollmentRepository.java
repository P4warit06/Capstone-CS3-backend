package com.example.demo.repository;

import com.example.demo.entity.RegistrarEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RegistrarEnrollmentRepository extends JpaRepository<RegistrarEnrollment, Integer> {
    List<RegistrarEnrollment> findByStudentId(String studentId);

    Optional<RegistrarEnrollment> findByEnrollmentIdAndStudentId(Integer enrollmentId, String studentId);

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

    @Query("""
            SELECT DISTINCT e.courseId FROM RegistrarEnrollment e
            WHERE e.studentId = :studentId
              AND e.status = 'registered'
              AND (e.academicYear < :academicYear
                   OR (e.academicYear = :academicYear AND e.semester < :semester))
            """)
    List<Integer> findRegisteredCourseIdsBeforeTerm(@Param("studentId") String studentId,
                                                    @Param("academicYear") Integer academicYear,
                                                    @Param("semester") Integer semester);

    @Query("""
            SELECT e FROM RegistrarEnrollment e
            WHERE e.studentId = :studentId
              AND e.status = 'registered'
              AND (e.academicYear < :academicYear
                   OR (e.academicYear = :academicYear AND e.semester < :semester))
            """)
    List<RegistrarEnrollment> findRegisteredEnrollmentsBeforeTerm(@Param("studentId") String studentId,
                                                                 @Param("academicYear") Integer academicYear,
                                                                 @Param("semester") Integer semester);

    default boolean existsCompletedEnrollment(String studentId, Integer courseId,
                                              Integer academicYear, Integer semester) {
        return findRegisteredCourseIdsBeforeTerm(studentId, academicYear, semester).contains(courseId);
    }

    boolean existsByStudentIdAndCourseId(String studentId, Integer courseId);
}
