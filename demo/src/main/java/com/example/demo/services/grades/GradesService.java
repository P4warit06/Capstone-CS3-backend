package com.example.demo.services.grades;

import com.example.demo.dtos.GradeDto;
import com.example.demo.dtos.GradeEnrollmentDto;
import com.example.demo.dtos.GradeRequestDto;
import com.example.demo.entity.RegistrarEnrollment;
import com.example.demo.entity.StudentCourseGrade;
import com.example.demo.entity.User;
import com.example.demo.repository.RegistrarEnrollmentRepository;
import com.example.demo.repository.StudentCourseGradeRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GradesService {
    private static final Set<String> ALLOWED_GRADES =
            Set.of("A", "B+", "B", "C+", "C", "D+", "D", "F", "S", "U");

    private final UserRepository userRepository;
    private final RegistrarEnrollmentRepository registrarEnrollmentRepository;
    private final StudentCourseGradeRepository studentCourseGradeRepository;

    @Transactional(readOnly = true)
    public List<GradeDto> getGrades(String email) {
        User user = findStudentUser(email);
        return studentCourseGradeRepository.findGradesByStudentId(user.getStudentId());
    }

    @Transactional(readOnly = true)
    public List<GradeEnrollmentDto> getRegisteredEnrollments(String email) {
        User user = findStudentUser(email);
        return studentCourseGradeRepository.findRegisteredEnrollmentsWithGrades(user.getStudentId());
    }

    @Transactional
    public GradeDto updateGrade(String email, GradeRequestDto request) {
        if (request == null || request.getEnrollmentId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "enrollmentId is required");
        }
        if (request.getGrade() == null || !ALLOWED_GRADES.contains(request.getGrade())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "grade must be one of: A, B+, B, C+, C, D+, D, F, S, U");
        }

        User user = findStudentUser(email);
        String studentId = user.getStudentId();
        RegistrarEnrollment enrollment = registrarEnrollmentRepository
                .findByEnrollmentIdAndStudentId(request.getEnrollmentId(), studentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enrollment not found"));
        if (!"registered".equalsIgnoreCase(enrollment.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Grade can only be set for registered enrollments");
        }

        StudentCourseGrade grade = studentCourseGradeRepository
                .findFirstByEnrollmentIdOrderByUpdatedAtDescGradeIdDesc(enrollment.getEnrollmentId())
                .orElseGet(StudentCourseGrade::new);
        grade.setEnrollmentId(enrollment.getEnrollmentId());
        grade.setGrade(request.getGrade());
        grade.setUserId(user.getId());
        studentCourseGradeRepository.saveAndFlush(grade);
        return studentCourseGradeRepository.findGradesByStudentId(studentId).stream()
                .filter(item -> item.getEnrollmentId().equals(enrollment.getEnrollmentId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Saved grade could not be retrieved"));
    }

    private User findStudentUser(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getStudentId() == null || user.getStudentId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not linked to a student record");
        }
        return user;
    }
}
