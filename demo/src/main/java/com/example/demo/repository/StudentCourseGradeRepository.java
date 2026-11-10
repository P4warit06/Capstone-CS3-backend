package com.example.demo.repository;

import com.example.demo.dtos.GradeDto;
import com.example.demo.dtos.GradeEnrollmentDto;
import com.example.demo.entity.StudentCourseGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource(exported = false)
public interface StudentCourseGradeRepository extends JpaRepository<StudentCourseGrade, Integer> {
    Optional<StudentCourseGrade> findFirstByEnrollmentIdOrderByUpdatedAtDescGradeIdDesc(Integer enrollmentId);

    @Query(value = """
            SELECT e.enrollment_id AS "enrollmentId",
                   e.course_id AS "courseId",
                   c.course_code AS "courseCode",
                   c.name_th AS "nameTh",
                   c.name_en AS "nameEn",
                   e.academic_year AS "academicYear",
                   e.semester AS "semester",
                   e.status AS "enrollmentStatus",
                   (
                     SELECT g.grade
                     FROM student_course_grades g
                     WHERE g.enrollment_id = e.enrollment_id
                     ORDER BY g.updated_at DESC, g.grade_id DESC
                     LIMIT 1
                   ) AS "currentGrade"
            FROM registrar_enrollments e
            JOIN courses c ON c.course_id = e.course_id
            WHERE e.student_id = :studentId
              AND LOWER(e.status) = 'registered'
            ORDER BY e.academic_year DESC, e.semester DESC, c.course_code ASC, e.enrollment_id ASC
            """, nativeQuery = true)
    List<GradeEnrollmentDto> findRegisteredEnrollmentsWithGrades(@Param("studentId") String studentId);

    @Query(value = """
            SELECT g.enrollment_id AS "enrollmentId",
                   e.course_id AS "courseId",
                   c.course_code AS "courseCode",
                   g.grade AS "grade",
                   e.academic_year AS "academicYear",
                   e.semester AS "semester"
            FROM student_course_grades g
            JOIN registrar_enrollments e ON e.enrollment_id = g.enrollment_id
            JOIN courses c ON c.course_id = e.course_id
            WHERE e.student_id = :studentId
              AND g.grade_id = (
                  SELECT latest.grade_id
                  FROM student_course_grades latest
                  WHERE latest.enrollment_id = g.enrollment_id
                  ORDER BY latest.updated_at DESC, latest.grade_id DESC
                  LIMIT 1
              )
            ORDER BY e.academic_year DESC, e.semester DESC, c.course_code ASC, e.enrollment_id ASC
            """, nativeQuery = true)
    List<GradeDto> findGradesByStudentId(@Param("studentId") String studentId);
}
