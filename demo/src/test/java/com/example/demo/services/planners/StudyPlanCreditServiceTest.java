package com.example.demo.services.planners;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StudyPlanCreditServiceTest {

    private final StudyPlanCreditService service = new StudyPlanCreditService();

    @Test
    void calculatesCreditsByCategoryAndExcludesNonCreditStatuses() {
        StudyPlanCreditService.CreditSummary summary = service.calculate(List.of(
                course(3, "IT_FOUNDATION", "ITF101", "IT Foundation", false, "completed"),
                course(3, "GE", "GEN201", "General Education", false, "completed"),
                course(3, "FREE_ELECTIVE", "GEN202", "General Elective", false, "planned"),
                course(6, "MAJOR_ADVANCED", "ADV301", "Advanced Track", true, "planned"),
                course(3, "PROJECT", "INT491", "Capstone Project", false, "planned"),
                course(3, "GE", "GEN203", "Withdrawn", false, "withdrawn"),
                course(3, "FREE_ELECTIVE", "ELC101", "Not Registered", false, "not_registered")
        ));

        assertEquals(18, summary.totalCredits());
        assertEquals(3, summary.itFoundationCredits());
        assertEquals(3, summary.generalEducationCredits());
        assertEquals(6, summary.trackCredits());
        assertEquals(3, summary.projectAndWorkCredits());
        assertEquals(3, summary.freeElectiveCredits());
    }

    @Test
    void prioritizesTrackMembershipAndProjectNameOverCoursePrefix() {
        StudyPlanCreditService.CreditSummary summary = service.calculate(List.of(
                course(3, "GE", "GEN301", "General Education", true, "completed"),
                course(3, "OTHER", "INT491", "Capstone Project", false, "completed")
        ));

        assertEquals(6, summary.totalCredits());
        assertEquals(3, summary.trackCredits());
        assertEquals(3, summary.projectAndWorkCredits());
        assertEquals(0, summary.generalEducationCredits());
        assertEquals(0, summary.itFoundationCredits());
    }

    private StudyPlanCreditService.CourseCredit course(int credits, String categoryCode, String courseCode,
                                                        String name, boolean trackCourse, String status) {
        return new StudyPlanCreditService.CourseCredit(credits, categoryCode, courseCode,
                name, name, trackCourse, status);
    }
}
