package com.example.demo.services.planners;

import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Locale;

@Service
public class StudyPlanCreditService {

    public CreditSummary calculate(Collection<CourseCredit> courses) {
        int totalCredits = 0;
        int itFoundationCredits = 0;
        int generalEducationCredits = 0;
        int trackCredits = 0;
        int projectAndWorkCredits = 0;
        int freeElectiveCredits = 0;

        for (CourseCredit course : courses) {
            if (!countsTowardCredits(course.status())) {
                continue;
            }

            totalCredits += course.credits();
            switch (classify(course)) {
                case IT_FOUNDATION -> itFoundationCredits += course.credits();
                case GENERAL_EDUCATION -> generalEducationCredits += course.credits();
                case TRACK -> trackCredits += course.credits();
                case PROJECT_AND_WORK -> projectAndWorkCredits += course.credits();
                case FREE_ELECTIVE -> freeElectiveCredits += course.credits();
                case OTHER -> {
                }
            }
        }

        return new CreditSummary(totalCredits, itFoundationCredits, generalEducationCredits,
                trackCredits, projectAndWorkCredits, freeElectiveCredits);
    }

    private boolean countsTowardCredits(String status) {
        return !"withdrawn".equalsIgnoreCase(status) && !"not_registered".equalsIgnoreCase(status);
    }

    private CreditCategory classify(CourseCredit course) {
        String categoryCode = normalized(course.categoryCode());
        String courseCode = normalized(course.courseCode());
        String courseName = normalized(course.nameTh()) + " " + normalized(course.nameEn());

        if (course.trackCourse() || "MAJOR_ADVANCED".equals(categoryCode)) {
            return CreditCategory.TRACK;
        }
        if ("GE".equals(categoryCode)) {
            return CreditCategory.GENERAL_EDUCATION;
        }
        if ("FREE_ELECTIVE".equals(categoryCode)) {
            return CreditCategory.FREE_ELECTIVE;
        }
        if (isProjectOrWork(categoryCode) || isProjectOrWork(courseCode) || isProjectOrWork(courseName)) {
            return CreditCategory.PROJECT_AND_WORK;
        }
        if (categoryCode.contains("FOUNDATION") || courseCode.startsWith("INT")
                || courseName.contains("IT FOUNDATION")) {
            return CreditCategory.IT_FOUNDATION;
        }
        return CreditCategory.OTHER;
    }

    private boolean isProjectOrWork(String value) {
        return value.contains("PROJECT") || value.contains("INTERNSHIP")
                || value.contains("COOP") || value.contains("PRACTICUM")
                || value.contains("WORK") || value.contains("โครงงาน")
                || value.contains("ฝึกงาน") || value.contains("สหกิจ");
    }

    private String normalized(String value) {
        return value == null ? "" : value.toUpperCase(Locale.ROOT);
    }

    public record CourseCredit(int credits, String categoryCode, String courseCode,
                               String nameTh, String nameEn, boolean trackCourse, String status) {}

    public record CreditSummary(int totalCredits, int itFoundationCredits,
                                int generalEducationCredits, int trackCredits,
                                int projectAndWorkCredits, int freeElectiveCredits) {}

    private enum CreditCategory {
        IT_FOUNDATION,
        GENERAL_EDUCATION,
        TRACK,
        PROJECT_AND_WORK,
        FREE_ELECTIVE,
        OTHER
    }
}
