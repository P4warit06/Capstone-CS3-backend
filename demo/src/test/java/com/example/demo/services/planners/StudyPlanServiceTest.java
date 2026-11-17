package com.example.demo.services.planners;

import com.example.demo.entity.StudyPlan;
import com.example.demo.entity.User;
import com.example.demo.repository.CurriculumCategoryCourseRepository;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.RegistrarEnrollmentRepository;
import com.example.demo.repository.RegistrarStudentRepository;
import com.example.demo.repository.StudentCourseGradeRepository;
import com.example.demo.repository.StudyPlanCourseRepository;
import com.example.demo.repository.StudyPlanRepository;
import com.example.demo.repository.TrackCourseRepository;
import com.example.demo.repository.TrackRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudyPlanServiceTest {

    @Mock
    private StudyPlanRepository studyPlanRepository;

    @Mock
    private RegistrarStudentRepository registrarStudentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TrackRepository trackRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CurriculumCategoryCourseRepository curriculumCategoryCourseRepository;

    @Mock
    private TrackCourseRepository trackCourseRepository;

    @Mock
    private StudyPlanCourseRepository studyPlanCourseRepository;

    @Mock
    private RegistrarEnrollmentRepository registrarEnrollmentRepository;

    @Mock
    private StudentCourseGradeRepository studentCourseGradeRepository;

    @Mock
    private StudyPlanCreditService studyPlanCreditService;

    @Mock
    private PrerequisiteValidationService prerequisiteValidationService;

    private StudyPlanService studyPlanService;

    @BeforeEach
    void setUp() {
        studyPlanService = new StudyPlanService(
                studyPlanRepository,
                registrarStudentRepository,
                userRepository,
                trackRepository,
                courseRepository,
                curriculumCategoryCourseRepository,
                trackCourseRepository,
                studyPlanCourseRepository,
                registrarEnrollmentRepository,
                studentCourseGradeRepository,
                studyPlanCreditService,
                prerequisiteValidationService);
    }

    @Test
    void clearsAndFlushesTheDeletedDefaultBeforePromotingItsReplacement() {
        User user = new User();
        user.setId(8);
        StudyPlan defaultPlan = plan(10, true);
        StudyPlan replacement = plan(11, false);

        when(userRepository.findByEmailIgnoreCase("student@example.com")).thenReturn(Optional.of(user));
        when(studyPlanRepository.findOwnedPlanById(10, 8)).thenReturn(Optional.of(defaultPlan));
        when(studyPlanRepository.findByUserIdCustomOrder(8)).thenReturn(List.of(defaultPlan, replacement));
        when(studyPlanCourseRepository.findByStudyPlanStudyPlanId(10)).thenReturn(List.of());

        studyPlanService.deletePlan("student@example.com", 10);

        assertFalse(defaultPlan.getIsDefault());
        assertTrue(replacement.getIsDefault());
        InOrder order = inOrder(studyPlanRepository, studyPlanCourseRepository);
        order.verify(studyPlanRepository).flush();
        order.verify(studyPlanCourseRepository).deleteAll(List.of());
        order.verify(studyPlanRepository).delete(defaultPlan);
        order.verify(studyPlanRepository).flush();
        verify(studyPlanRepository).findOwnedPlanById(10, 8);
    }

    @Test
    void clearsAndFlushesTheOldDefaultBeforeSettingTheNewOne() {
        User user = new User();
        user.setId(8);
        StudyPlan oldDefault = plan(10, true);
        StudyPlan target = plan(11, false);

        when(userRepository.findByEmailIgnoreCase("student@example.com")).thenReturn(Optional.of(user));
        when(studyPlanRepository.findOwnedPlanById(11, 8)).thenReturn(Optional.of(target));
        when(studyPlanRepository.findByUserIdCustomOrder(8)).thenReturn(List.of(oldDefault, target));

        List<Long> defaultCountAtFlush = new ArrayList<>();
        doAnswer(invocation -> {
            defaultCountAtFlush.add(List.of(oldDefault, target).stream()
                    .filter(p -> Boolean.TRUE.equals(p.getIsDefault())).count());
            return null;
        }).when(studyPlanRepository).flush();

        var result = studyPlanService.setDefaultPlan("student@example.com", 11);

        assertFalse(oldDefault.getIsDefault());
        assertTrue(target.getIsDefault());
        assertTrue(result.getIsDefault());
        assertEquals(List.of(0L, 1L), defaultCountAtFlush);
    }

    @Test
    void settingTheCurrentDefaultAgainDoesNothing() {
        User user = new User();
        user.setId(8);
        StudyPlan currentDefault = plan(10, true);

        when(userRepository.findByEmailIgnoreCase("student@example.com")).thenReturn(Optional.of(user));
        when(studyPlanRepository.findOwnedPlanById(10, 8)).thenReturn(Optional.of(currentDefault));

        var result = studyPlanService.setDefaultPlan("student@example.com", 10);

        assertTrue(result.getIsDefault());
        verify(studyPlanRepository, never()).flush();
    }

    private StudyPlan plan(int planId, boolean isDefault) {
        StudyPlan plan = new StudyPlan();
        plan.setStudyPlanId(planId);
        plan.setIsDefault(isDefault);
        return plan;
    }
}
