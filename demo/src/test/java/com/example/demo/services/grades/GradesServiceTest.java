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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GradesServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private RegistrarEnrollmentRepository registrarEnrollmentRepository;

    @Mock
    private StudentCourseGradeRepository studentCourseGradeRepository;

    private GradesService gradesService;
    private User user;

    @BeforeEach
    void setUp() {
        gradesService = new GradesService(
                userRepository, registrarEnrollmentRepository, studentCourseGradeRepository);
        user = new User();
        user.setId(8);
        user.setEmail("student@example.com");
        user.setStudentId("034");
        when(userRepository.findByEmailIgnoreCase("student@example.com")).thenReturn(Optional.of(user));
    }

    @Test
    void returnsRegisteredEnrollmentsWithExistingGrades() {
        GradeEnrollmentDto enrollment = org.mockito.Mockito.mock(GradeEnrollmentDto.class);
        when(studentCourseGradeRepository.findRegisteredEnrollmentsWithGrades("034"))
                .thenReturn(List.of(enrollment));

        List<GradeEnrollmentDto> result = gradesService.getRegisteredEnrollments("student@example.com");

        assertSame(enrollment, result.getFirst());
        verify(studentCourseGradeRepository).findRegisteredEnrollmentsWithGrades("034");
    }

    @Test
    void updatesTheLatestExistingGradeForRegisteredEnrollment() {
        RegistrarEnrollment enrollment = new RegistrarEnrollment();
        enrollment.setEnrollmentId(214);
        enrollment.setStatus("registered");
        when(registrarEnrollmentRepository.findByEnrollmentIdAndStudentId(214, "034"))
                .thenReturn(Optional.of(enrollment));
        StudentCourseGrade existingGrade = new StudentCourseGrade();
        when(studentCourseGradeRepository.findFirstByEnrollmentIdOrderByUpdatedAtDescGradeIdDesc(214))
                .thenReturn(Optional.of(existingGrade));
        GradeDto response = org.mockito.Mockito.mock(GradeDto.class);
        when(response.getEnrollmentId()).thenReturn(214);
        when(studentCourseGradeRepository.findGradesByStudentId("034")).thenReturn(List.of(response));
        GradeRequestDto request = new GradeRequestDto();
        request.setEnrollmentId(214);
        request.setGrade("A");

        GradeDto result = gradesService.updateGrade("student@example.com", request);

        assertEquals(214, existingGrade.getEnrollmentId());
        assertEquals("A", existingGrade.getGrade());
        assertEquals(8, existingGrade.getUserId());
        verify(studentCourseGradeRepository).saveAndFlush(existingGrade);
        assertSame(response, result);
    }

    @Test
    void rejectsGradeUpdateWhenEnrollmentIsNotRegistered() {
        RegistrarEnrollment enrollment = new RegistrarEnrollment();
        enrollment.setEnrollmentId(214);
        enrollment.setStatus("completed");
        when(registrarEnrollmentRepository.findByEnrollmentIdAndStudentId(214, "034"))
                .thenReturn(Optional.of(enrollment));
        GradeRequestDto request = new GradeRequestDto();
        request.setEnrollmentId(214);
        request.setGrade("A");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> gradesService.updateGrade("student@example.com", request));

        assertEquals(409, exception.getStatusCode().value());
        verify(studentCourseGradeRepository, never()).saveAndFlush(any(StudentCourseGrade.class));
    }
}
