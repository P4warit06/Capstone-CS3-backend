package com.example.demo.services.planners;

import com.example.demo.dtos.AddPlanCourseRequestDto;
import com.example.demo.dtos.CreatePlanRequestDto;
import com.example.demo.dtos.MovePlanCourseRequestDto;
import com.example.demo.dtos.PlanDto;
import com.example.demo.dtos.PlanListResponseDto;
import com.example.demo.dtos.PlanTrackDto;
import com.example.demo.dtos.StudyPlanDetailDto;
import com.example.demo.dtos.UpdatePlanRequestDto;
import com.example.demo.entity.Category;
import com.example.demo.entity.Course;
import com.example.demo.entity.CurriculumCategoryCourse;
import com.example.demo.entity.RegistrarEnrollment;
import com.example.demo.entity.RegistrarStudent;
import com.example.demo.entity.StudyPlan;
import com.example.demo.entity.StudyPlanCourse;
import com.example.demo.entity.TrackCourse;
import com.example.demo.entity.User;
import com.example.demo.exceptions.PlanLimitExceededException;
import com.example.demo.repository.CurriculumCategoryCourseRepository;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.RegistrarEnrollmentRepository;
import com.example.demo.repository.RegistrarStudentRepository;
import com.example.demo.repository.StudyPlanCourseRepository;
import com.example.demo.repository.StudyPlanRepository;
import com.example.demo.repository.TrackCourseRepository;
import com.example.demo.repository.TrackRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudyPlanService {

    private final StudyPlanRepository studyPlanRepository;
    private final RegistrarStudentRepository registrarStudentRepository;
    private final UserRepository userRepository;
    private final TrackRepository trackRepository;
    private final CourseRepository courseRepository;
    private final CurriculumCategoryCourseRepository curriculumCategoryCourseRepository;
    private final TrackCourseRepository trackCourseRepository;
    private final StudyPlanCourseRepository studyPlanCourseRepository;
    private final RegistrarEnrollmentRepository registrarEnrollmentRepository;
    private final StudyPlanCreditService studyPlanCreditService;

    @Transactional(readOnly = true)
    public StudyPlanDetailDto getPlanById(String email, Integer planId) {
        User currentUser = findUserByEmail(email);
        StudyPlan plan = studyPlanRepository.findOwnedPlanById(planId, currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Study plan not found"));
        RegistrarStudent student = findRegistrarStudent(currentUser);

        Integer curriculumVersionId = plan.getCurriculumVersion().getCurriculumVersionId();
        List<CurriculumCategoryCourse> allCurriculumCourses = curriculumCategoryCourseRepository
                .findByCurriculumCategoryCurriculumVersionCurriculumVersionId(curriculumVersionId);
        List<CurriculumCategoryCourse> curriculumCourses = allCurriculumCourses.stream()
                .filter(CurriculumCategoryCourse::getIsRequired)
                .toList();
        List<TrackCourse> trackCourses = plan.getTrack() == null
                ? List.of()
                : trackCourseRepository.findByTrackTrackId(plan.getTrack().getTrackId());
        List<TrackCourse> requiredTrackCourses = trackCourses.stream()
                .filter(mapping -> Boolean.TRUE.equals(mapping.getIsRequired()))
                .toList();
        Set<Integer> trackCourseIds = trackCourses.stream()
                .map(mapping -> mapping.getCourse().getCourseId())
                .collect(Collectors.toSet());
        List<StudyPlanCourse> userCourses = studyPlanCourseRepository.findByStudyPlanStudyPlanId(planId);
        List<RegistrarEnrollment> enrollments = registrarEnrollmentRepository.findByStudentId(student.getStudentId());

        Map<Integer, Category> categoriesByCourse = new HashMap<>();
        Map<String, Category> categoriesByCode = new HashMap<>();
        for (CurriculumCategoryCourse mapping : allCurriculumCourses) {
            Category category = mapping.getCurriculumCategory().getCategory();
            categoriesByCourse.putIfAbsent(mapping.getCourse().getCourseId(), category);
            categoriesByCode.put(category.getCode(), category);
        }

        Map<Integer, List<RegistrarEnrollment>> enrollmentsByCourse = enrollments.stream()
                .collect(Collectors.groupingBy(RegistrarEnrollment::getCourseId));
        Map<PlacementKey, StudyPlanDetailDto.Course> placements = new HashMap<>();
        Set<Integer> representedEnrollmentIds = new HashSet<>();
        Set<Integer> requiredCourseIds = new HashSet<>();

        Category majorAdvanced = categoriesByCode.get("MAJOR_ADVANCED");
        for (CurriculumCategoryCourse mapping : curriculumCourses) {
            Course course = mapping.getCourse();
            if (!requiredCourseIds.add(course.getCourseId())) {
                continue;
            }
            String trackLabel = trackCourseIds.contains(course.getCourseId()) && plan.getTrack() != null
                    ? plan.getTrack().getTrackNameEn() : null;
            addRequiredCourse(placements, representedEnrollmentIds, enrollmentsByCourse, course,
                    categoriesByCourse.get(course.getCourseId()), trackLabel, mapping.getStandardYear(),
                    mapping.getStandardSemester(), student);
        }
        for (TrackCourse mapping : requiredTrackCourses) {
            Course course = mapping.getCourse();
            if (!requiredCourseIds.add(course.getCourseId())) {
                continue;
            }
            addRequiredCourse(placements, representedEnrollmentIds, enrollmentsByCourse, course,
                    majorAdvanced, plan.getTrack().getTrackNameEn(), mapping.getStandardYear(),
                    mapping.getStandardSemester(), student);
        }

        for (StudyPlanCourse plannedCourse : userCourses) {
            Course course = plannedCourse.getCourse();
            int year = plannedCourse.getStudyYear();
            int semester = plannedCourse.getStudySemester();
            List<RegistrarEnrollment> matchingEnrollments = enrollmentsByCourse
                    .getOrDefault(course.getCourseId(), List.of()).stream()
                    .filter(enrollment -> enrollmentStudyYear(enrollment, student) == year
                            && enrollment.getSemester() == semester)
                    .toList();
            String status = matchingEnrollments.isEmpty()
                    ? (isPast(year, semester, student) ? "not_registered" : "planned")
                    : matchingEnrollments.getFirst().getStatus();
            matchingEnrollments.forEach(enrollment -> representedEnrollmentIds.add(enrollment.getEnrollmentId()));
            String trackLabel = trackCourseIds.contains(course.getCourseId()) && plan.getTrack() != null
                    ? plan.getTrack().getTrackNameEn() : null;
            placements.putIfAbsent(new PlacementKey(course.getCourseId(), year, semester),
                    toCourseDto(course, "user", false, categoriesByCourse.get(course.getCourseId()), status,
                            plannedCourse.getStudyPlanCourseId(), trackLabel));
        }

        for (RegistrarEnrollment enrollment : enrollments) {
            if (representedEnrollmentIds.contains(enrollment.getEnrollmentId())) {
                continue;
            }
            int year = enrollmentStudyYear(enrollment, student);
            Course course = findCourseForEnrollment(enrollment, curriculumCourses, trackCourses, userCourses);
            String trackLabel = trackCourseIds.contains(course.getCourseId()) && plan.getTrack() != null
                    ? plan.getTrack().getTrackNameEn() : null;
            placements.putIfAbsent(new PlacementKey(course.getCourseId(), year, enrollment.getSemester()),
                    toCourseDto(course, "registrar", false, categoriesByCourse.get(course.getCourseId()),
                            enrollment.getStatus(), null, trackLabel));
        }

        int maxYear = Math.max(4, placements.keySet().stream().mapToInt(PlacementKey::year).max().orElse(4));
        List<StudyPlanDetailDto.Year> years = new ArrayList<>();
        for (int year = 1; year <= maxYear; year++) {
            List<StudyPlanDetailDto.Semester> semesters = new ArrayList<>();
            int yearCredits = 0;
            for (int semester = 1; semester <= 2; semester++) {
                final int currentYear = year;
                final int currentSemester = semester;
                List<StudyPlanDetailDto.Course> courses = placements.entrySet().stream()
                        .filter(entry -> entry.getKey().year() == currentYear
                                && entry.getKey().semester() == currentSemester)
                        .map(Map.Entry::getValue)
                        .sorted(Comparator.comparing(StudyPlanDetailDto.Course::getLocked).reversed()
                                .thenComparing(StudyPlanDetailDto.Course::getCourseCode))
                        .toList();
                StudyPlanCreditService.CreditSummary creditSummary = studyPlanCreditService.calculate(
                        courses.stream()
                                .map(course -> new StudyPlanCreditService.CourseCredit(
                                        course.getCredits(),
                                        course.getCategory() == null ? null : course.getCategory().getCode(),
                                        course.getCourseCode(),
                                        course.getNameTh(),
                                        course.getNameEn(),
                                        "track".equals(course.getSource())
                                                || trackCourseIds.contains(course.getCourseId()),
                                        course.getStatus()))
                                .toList());
                int credits = creditSummary.totalCredits();
                yearCredits += credits;
                semesters.add(StudyPlanDetailDto.Semester.builder()
                        .semester(semester)
                        .status(getTermStatus(year, semester, student))
                        .totalCredits(credits)
                        .itFoundationCredits(creditSummary.itFoundationCredits())
                        .generalEducationCredits(creditSummary.generalEducationCredits())
                        .trackCredits(creditSummary.trackCredits())
                        .projectAndWorkCredits(creditSummary.projectAndWorkCredits())
                        .freeElectiveCredits(creditSummary.freeElectiveCredits())
                        .courses(courses)
                        .build());
            }
            years.add(StudyPlanDetailDto.Year.builder()
                    .year(year)
                    .academicYear(student.getAdmissionYear() + year - 1)
                    .status(year < student.getCurrentYear() ? "completed"
                            : Objects.equals(year, student.getCurrentYear()) ? "current" : "upcoming")
                    .totalCredits(yearCredits)
                    .semesters(semesters)
                    .build());
        }

        PlanTrackDto track = plan.getTrack() == null ? null : PlanTrackDto.builder()
                .id(plan.getTrack().getTrackId())
                .nameTh(plan.getTrack().getTrackNameTh())
                .nameEn(plan.getTrack().getTrackNameEn())
                .build();

        return StudyPlanDetailDto.builder()
                .studyPlanId(plan.getStudyPlanId())
                .planName(plan.getPlanName())
                .isDefault(plan.getIsDefault())
                .student(StudyPlanDetailDto.Student.builder()
                        .studentId(student.getStudentId())
                        .admissionYear(student.getAdmissionYear())
                        .currentYear(student.getCurrentYear())
                        .currentSemester(student.getCurrentSemester())
                        .build())
                .curriculum(StudyPlanDetailDto.Curriculum.builder()
                        .id(curriculumVersionId)
                        .name(plan.getCurriculumVersion().getVersionName())
                        .totalCredits(plan.getCurriculumVersion().getTotalCredits())
                        .build())
                .track(track)
                .years(years)
                .build();
    }

    /** ปีการศึกษาที่วางแผนได้สูงสุด (ป.ตรี 4 ปี เรียนได้ไม่เกิน 8 ปี) — ตรงกับปุ่ม "เพิ่มปีการศึกษา" ฝั่ง FE */
    private static final int MAX_STUDY_YEAR = 8;

    /**
     * SCRUM-171: เพิ่มวิชาที่นักศึกษาเลือกเองลงแผน (study_plan_courses)
     * เพิ่มได้เฉพาะเทอมที่ยังไม่ถึง และกันวิชาที่อยู่ในแผนแล้ว / ลงทะเบียนไปแล้ว
     */
    @Transactional
    public StudyPlanDetailDto.Course addCourseToPlan(String email, Integer planId, AddPlanCourseRequestDto request) {
        User currentUser = findUserByEmail(email);
        StudyPlan plan = studyPlanRepository.findOwnedPlanById(planId, currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ไม่พบแผนการเรียน"));
        RegistrarStudent student = findRegistrarStudent(currentUser);

        // 1) ตรวจ body
        if (request == null || request.getCourseId() == null
                || request.getStudyYear() == null || request.getStudySemester() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "ต้องระบุ courseId, studyYear และ studySemester");
        }
        int year = request.getStudyYear();
        int semester = request.getStudySemester();
        if (year < 1 || year > MAX_STUDY_YEAR || semester < 1 || semester > 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "ปีต้องอยู่ระหว่าง 1-" + MAX_STUDY_YEAR + " และภาคเรียนต้องเป็น 1 หรือ 2");
        }
        if (!"upcoming".equals(getTermStatus(year, semester, student))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "เพิ่มวิชาได้เฉพาะภาคเรียนที่ยังไม่ถึง");
        }

        Course course = courseRepository.findByCourseId(request.getCourseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ไม่พบรายวิชา"));
        if (!Boolean.TRUE.equals(course.getIsActive())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "รายวิชานี้ปิดให้บริการแล้ว");
        }
        Integer courseId = course.getCourseId();

        // 2) ลงทะเบียนไปแล้ว (ผ่านแล้ว / กำลังเรียน) — W หรือติด F/U ยังเพิ่มเพื่อลงใหม่ได้
        if (registrarEnrollmentRepository.existsActiveEnrollment(student.getStudentId(), courseId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "ลงทะเบียนวิชานี้ไปแล้ว");
        }

        // 3) เพิ่มเองไว้ในแผนแล้ว ในเทอมที่ยังไม่ผ่าน
        boolean alreadyPlanned = studyPlanCourseRepository.findByStudyPlanStudyPlanId(planId).stream()
                .anyMatch(pc -> pc.getCourse().getCourseId().equals(courseId)
                        && !isPast(pc.getStudyYear(), pc.getStudySemester(), student));
        if (alreadyPlanned) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "มีวิชานี้ในแผนแล้ว");
        }

        // 4) วิชาบังคับ (หลักสูตร / track ของแผน) ที่ยังไม่เคยลงและเทอมมาตรฐานยังไม่ผ่าน = อยู่ในแผนอัตโนมัติแล้ว
        //    ถ้าเทอมมาตรฐานผ่านไปแล้วแต่ไม่ได้ลง (not_registered) หรือเคย W/F ให้เพิ่มเพื่อวางแผนลงใหม่ได้
        Integer curriculumVersionId = plan.getCurriculumVersion().getCurriculumVersionId();
        List<CurriculumCategoryCourse> curriculumMappings = curriculumCategoryCourseRepository
                .findByCurriculumCategoryCurriculumVersionCurriculumVersionId(curriculumVersionId);
        boolean neverEnrolled = !registrarEnrollmentRepository
                .existsByStudentIdAndCourseId(student.getStudentId(), courseId);
        if (neverEnrolled) {
            boolean requiredUpcoming = curriculumMappings.stream()
                    .anyMatch(m -> Boolean.TRUE.equals(m.getIsRequired())
                            && m.getCourse().getCourseId().equals(courseId)
                            && m.getStandardYear() != null && m.getStandardSemester() != null
                            && !isPast(m.getStandardYear(), m.getStandardSemester(), student));
            if (!requiredUpcoming && plan.getTrack() != null) {
                requiredUpcoming = trackCourseRepository
                        .findByTrackTrackIdAndIsRequiredTrue(plan.getTrack().getTrackId()).stream()
                        .anyMatch(m -> m.getCourse().getCourseId().equals(courseId)
                                && m.getStandardYear() != null && m.getStandardSemester() != null
                                && !isPast(m.getStandardYear(), m.getStandardSemester(), student));
            }
            if (requiredUpcoming) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "เป็นวิชาบังคับที่อยู่ในแผนแล้ว");
            }
        }

        // 5) บันทึก + อัปเดตเวลาแก้ไขล่าสุดของแผน
        StudyPlanCourse planCourse = new StudyPlanCourse();
        planCourse.setStudyPlan(plan);
        planCourse.setCourse(course);
        planCourse.setStudyYear(year);
        planCourse.setStudySemester(semester);
        try {
            planCourse = studyPlanCourseRepository.saveAndFlush(planCourse);
        } catch (DataIntegrityViolationException e) {
            // กดซ้ำพร้อมกัน → ชน unique index (plan, course, year, semester)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "มีวิชานี้ในแผนแล้ว");
        }
        plan.setUpdatedAt(LocalDateTime.now());
        studyPlanRepository.save(plan);

        Category category = curriculumMappings.stream()
                .filter(m -> m.getCourse().getCourseId().equals(courseId))
                .map(m -> m.getCurriculumCategory().getCategory())
                .findFirst()
                .orElse(null);
        return toCourseDto(course, "user", false, category, "planned", planCourse.getStudyPlanCourseId(), null);
    }

    /** SCRUM-172: ย้ายวิชาที่ผู้ใช้เพิ่มเองไปยังภาคเรียนในอนาคต */
    @Transactional
    public StudyPlanDetailDto.Course moveCourseInPlan(
            String email, Integer planId, Integer studyPlanCourseId, MovePlanCourseRequestDto request) {
        User currentUser = findUserByEmail(email);
        StudyPlan plan = studyPlanRepository.findOwnedPlanById(planId, currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ไม่พบแผนการเรียน"));

        if (request == null || request.getStudyYear() == null || request.getStudySemester() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "ต้องระบุ studyYear และ studySemester");
        }
        int destinationYear = request.getStudyYear();
        int destinationSemester = request.getStudySemester();
        if (destinationYear < 1 || destinationYear > MAX_STUDY_YEAR
                || destinationSemester < 1 || destinationSemester > 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "ปีต้องอยู่ระหว่าง 1-8 และภาคเรียนต้องเป็น 1 หรือ 2");
        }

        // Query must include planId so a course ID from another plan is indistinguishable from a missing one.
        StudyPlanCourse planCourse = studyPlanCourseRepository
                .findByStudyPlanCourseIdAndStudyPlanStudyPlanId(studyPlanCourseId, planId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ไม่พบวิชานี้ในแผน"));
        RegistrarStudent student = findRegistrarStudent(currentUser);
        int originalYear = planCourse.getStudyYear();
        int originalSemester = planCourse.getStudySemester();

        if (!"upcoming".equals(getTermStatus(originalYear, originalSemester, student))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "ย้ายได้เฉพาะวิชาในภาคเรียนที่ยังไม่ถึง");
        }
        if (!"upcoming".equals(getTermStatus(destinationYear, destinationSemester, student))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "ย้ายได้เฉพาะไปภาคเรียนที่ยังไม่ถึง");
        }
        if (originalYear == destinationYear && originalSemester == destinationSemester) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "วิชานี้อยู่ภาคเรียนนี้อยู่แล้ว");
        }

        Integer courseId = planCourse.getCourse().getCourseId();
        if (studyPlanCourseRepository
                .existsByStudyPlanStudyPlanIdAndCourseCourseIdAndStudyYearAndStudySemester(
                        planId, courseId, destinationYear, destinationSemester)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "มีวิชานี้ในภาคเรียนที่เลือกแล้ว");
        }

        planCourse.setStudyYear(destinationYear);
        planCourse.setStudySemester(destinationSemester);
        try {
            planCourse = studyPlanCourseRepository.saveAndFlush(planCourse);
        } catch (DataIntegrityViolationException exception) {
            // Covers a concurrent move that races the existence check above.
            throw new ResponseStatusException(HttpStatus.CONFLICT, "มีวิชานี้ในภาคเรียนที่เลือกแล้ว");
        }

        plan.setUpdatedAt(LocalDateTime.now());
        studyPlanRepository.save(plan);

        Category category = curriculumCategoryCourseRepository
                .findByCurriculumCategoryCurriculumVersionCurriculumVersionId(
                        plan.getCurriculumVersion().getCurriculumVersionId())
                .stream()
                .filter(mapping -> mapping.getCourse().getCourseId().equals(courseId))
                .map(mapping -> mapping.getCurriculumCategory().getCategory())
                .findFirst()
                .orElse(null);
        return toCourseDto(planCourse.getCourse(), "user", false, category, "planned",
                planCourse.getStudyPlanCourseId(), null);
    }

    /** SCRUM-172: ลบวิชาที่ผู้ใช้เพิ่มเองได้ทุกภาคเรียน */
    @Transactional
    public void removeCourseFromPlan(String email, Integer planId, Integer studyPlanCourseId) {
        User currentUser = findUserByEmail(email);
        StudyPlan plan = studyPlanRepository.findOwnedPlanById(planId, currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ไม่พบแผนการเรียน"));

        StudyPlanCourse planCourse = studyPlanCourseRepository
                .findByStudyPlanCourseIdAndStudyPlanStudyPlanId(studyPlanCourseId, planId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ไม่พบวิชานี้ในแผน"));

        studyPlanCourseRepository.delete(planCourse);
        plan.setUpdatedAt(LocalDateTime.now());
        studyPlanRepository.save(plan);
    }

    private void addRequiredCourse(Map<PlacementKey, StudyPlanDetailDto.Course> placements,
                                   Set<Integer> representedEnrollmentIds,
                                   Map<Integer, List<RegistrarEnrollment>> enrollmentsByCourse,
                                   Course course, Category category, String trackLabel,
                                   Integer standardYear, Integer standardSemester, RegistrarStudent student) {
        List<RegistrarEnrollment> courseEnrollments = enrollmentsByCourse
                .getOrDefault(course.getCourseId(), List.of());
        if (!courseEnrollments.isEmpty()) {
            for (RegistrarEnrollment enrollment : courseEnrollments) {
                int year = enrollmentStudyYear(enrollment, student);
                representedEnrollmentIds.add(enrollment.getEnrollmentId());
                placements.putIfAbsent(new PlacementKey(course.getCourseId(), year, enrollment.getSemester()),
                        toCourseDto(course, trackLabel == null ? "curriculum" : "track", true,
                                category, enrollment.getStatus(), null, trackLabel));
            }
            return;
        }
        if (standardYear == null || standardYear < 1 || standardSemester == null
                || standardSemester < 1 || standardSemester > 2) {
            throw new IllegalStateException("Required course has no valid standard study term: " + course.getCourseCode());
        }
        String status = isPast(standardYear, standardSemester, student) ? "not_registered" : "planned";
        placements.putIfAbsent(new PlacementKey(course.getCourseId(), standardYear, standardSemester),
                toCourseDto(course, trackLabel == null ? "curriculum" : "track", true,
                        category, status, null, trackLabel));
    }

    private StudyPlanDetailDto.Course toCourseDto(Course course, String source, boolean locked,
                                                   Category category, String status, Integer planCourseId,
                                                   String trackLabel) {
        String categoryCode = category == null ? "FREE_ELECTIVE" : category.getCode();
        String categoryLabel = trackLabel != null ? trackLabel
                : category == null ? "เลือกเสรี" : category.getCategoryNameTh();
        return StudyPlanDetailDto.Course.builder()
                .courseId(course.getCourseId())
                .courseCode(course.getCourseCode())
                .nameTh(course.getNameTh())
                .nameEn(course.getNameEn())
                .credits(course.getCredits())
                .source(source)
                .locked(locked)
                .category(StudyPlanDetailDto.CourseCategory.builder()
                        .code(trackLabel == null ? categoryCode : "MAJOR_ADVANCED")
                        .label(categoryLabel)
                        .build())
                .status(status)
                .studyPlanCourseId(planCourseId)
                .build();
    }

    private Course findCourseForEnrollment(RegistrarEnrollment enrollment,
                                            List<CurriculumCategoryCourse> curriculumCourses,
                                            List<TrackCourse> trackCourses,
                                            List<StudyPlanCourse> userCourses) {
        return curriculumCourses.stream().map(CurriculumCategoryCourse::getCourse)
                .filter(course -> course.getCourseId().equals(enrollment.getCourseId())).findFirst()
                .or(() -> trackCourses.stream().map(TrackCourse::getCourse)
                        .filter(course -> course.getCourseId().equals(enrollment.getCourseId())).findFirst())
                .or(() -> userCourses.stream().map(StudyPlanCourse::getCourse)
                        .filter(course -> course.getCourseId().equals(enrollment.getCourseId())).findFirst())
                .or(() -> courseRepository.findByCourseId(enrollment.getCourseId()))
                .orElseThrow(() -> new IllegalStateException("Enrollment course was not loaded: " + enrollment.getCourseId()));
    }

    private int enrollmentStudyYear(RegistrarEnrollment enrollment, RegistrarStudent student) {
        return enrollment.getAcademicYear() - student.getAdmissionYear() + 1;
    }

    private boolean isPast(int year, int semester, RegistrarStudent student) {
        return year < student.getCurrentYear()
                || (Objects.equals(year, student.getCurrentYear()) && semester < student.getCurrentSemester());
    }

    private String getTermStatus(int year, int semester, RegistrarStudent student) {
        if (isPast(year, semester, student)) {
            return "completed";
        }
        return Objects.equals(year, student.getCurrentYear())
                && Objects.equals(semester, student.getCurrentSemester()) ? "current" : "upcoming";
    }

    private record PlacementKey(Integer courseId, Integer year, Integer semester) {}

    @Transactional
    public PlanListResponseDto getUserPlansByEmail(String email) {
        User currentUser = findUserByEmail(email);
        return getUserPlans(currentUser);
    }

    @Transactional
    public PlanDto createPlanByEmail(String email, CreatePlanRequestDto request) {
        User currentUser = findUserByEmail(email);
        RegistrarStudent student = findRegistrarStudent(currentUser);

        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }
        String planName = request.getPlanName();
        if (planName == null || planName.isBlank() || planName.length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "planName must not be blank and must be at most 100 characters");
        }

        long planCount = studyPlanRepository.countByUserId(currentUser.getId());
        if (planCount >= 5) {
            throw new PlanLimitExceededException("สร้างแผนได้ไม่เกิน 5 แผน");
        }

        StudyPlan plan = new StudyPlan();
        plan.setUserId(currentUser.getId());
        plan.setCurriculumVersion(student.getCurriculumVersion());
        plan.setPlanName(planName);
        plan.setIsDefault(planCount == 0);

        if (request.getTrackId() != null) {
            var track = trackRepository.findById(request.getTrackId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Track not found"));
            if (!track.getCurriculumVersion().getCurriculumVersionId()
                    .equals(student.getCurriculumVersion().getCurriculumVersionId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Track does not belong to the student's curriculum");
            }
            plan.setTrack(track);
        }

        return mapToPlanDto(studyPlanRepository.save(plan));
    }

    @Transactional
    public PlanDto updatePlanName(String email, Integer planId, UpdatePlanRequestDto request) {
        User currentUser = findUserByEmail(email);
        StudyPlan plan = findOwnedPlan(currentUser, planId);
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }

        String planName = request.getPlanName();
        if (planName == null || planName.isBlank() || planName.length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "planName must not be blank and must be at most 100 characters");
        }

        plan.setPlanName(planName);
        return mapToPlanDto(studyPlanRepository.save(plan));
    }

    @Transactional
    public PlanDto setDefaultPlan(String email, Integer planId) {
        User currentUser = findUserByEmail(email);
        StudyPlan plan = findOwnedPlan(currentUser, planId);

        List<StudyPlan> userPlans = studyPlanRepository.findByUserIdCustomOrder(currentUser.getId());
        for (StudyPlan userPlan : userPlans) {
            userPlan.setIsDefault(userPlan.getStudyPlanId().equals(planId));
        }

        return mapToPlanDto(plan);
    }

    @Transactional
    public void deletePlan(String email, Integer planId) {
        User currentUser = findUserByEmail(email);
        StudyPlan plan = findOwnedPlan(currentUser, planId);
        List<StudyPlan> remainingPlans = studyPlanRepository.findByUserIdCustomOrder(currentUser.getId()).stream()
                .filter(userPlan -> !userPlan.getStudyPlanId().equals(planId))
                .toList();

        if (Boolean.TRUE.equals(plan.getIsDefault()) && !remainingPlans.isEmpty()) {
            remainingPlans.get(0).setIsDefault(true);
        }

        studyPlanCourseRepository.deleteAll(studyPlanCourseRepository.findByStudyPlanStudyPlanId(planId));
        studyPlanRepository.delete(plan);
    }

    @Transactional
    public PlanListResponseDto getUserPlans(User currentUser) {
        // 1. users.student_id -> registrar_students (ถ้าไม่มีส่ง 404)
        RegistrarStudent student = findRegistrarStudent(currentUser);

        // 2. ดึงรายการแผนการเรียนของผู้ใช้
        List<StudyPlan> plans = studyPlanRepository.findByUserIdCustomOrder(currentUser.getId());

        // 3. เคสยังไม่มีแผนเลย -> สร้าง "แผนที่ 1" ให้อัตโนมัติ
        if (plans.isEmpty()) {
            StudyPlan defaultPlan = new StudyPlan();
            defaultPlan.setUserId(currentUser.getId());
            defaultPlan.setCurriculumVersion(student.getCurriculumVersion());
            defaultPlan.setTrack(null);
            defaultPlan.setPlanName("แผนที่ 1");
            defaultPlan.setIsDefault(true);

            StudyPlan savedPlan = studyPlanRepository.save(defaultPlan);
            plans = List.of(savedPlan);
        }

        // 4. Transform เป็น DTO
        List<PlanDto> planDtos = plans.stream().map(this::mapToPlanDto).collect(Collectors.toList());

        return PlanListResponseDto.builder()
                .maxPlans(5)
                .plans(planDtos)
                .build();
    }

    private StudyPlan findOwnedPlan(User currentUser, Integer planId) {
        return studyPlanRepository.findOwnedPlanById(planId, currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Study plan not found"));
    }

    private PlanDto mapToPlanDto(StudyPlan plan) {
        PlanTrackDto planTrackDto = null;
        if (plan.getTrack() != null) {
            planTrackDto = PlanTrackDto.builder()
                    .id(plan.getTrack().getTrackId())
                    .nameTh(plan.getTrack().getTrackNameTh())
                    .nameEn(plan.getTrack().getTrackNameEn())
                    .build();
        }

        return PlanDto.builder()
                .studyPlanId(plan.getStudyPlanId())
                .planName(plan.getPlanName())
                .isDefault(plan.getIsDefault())
                .track(planTrackDto)
                .updatedAt(plan.getUpdatedAt())
                .build();
    }

    private User findUserByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token subject is missing");
        }
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private RegistrarStudent findRegistrarStudent(User currentUser) {
        if (currentUser.getStudentId() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student ID not found for this user");
        }
        return registrarStudentRepository.findByStudentId(currentUser.getStudentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Student not found in registrar system"));
    }

    @Transactional
    public StudyPlan ensureStudyPlanExists(Integer userId, Integer curriculumVersionId) {
        return studyPlanRepository.findByUserId(userId)
                .orElseGet(() -> {
                    StudyPlan newPlan = new StudyPlan();
                    newPlan.setUserId(userId);
                    // Assuming curriculumVersion is needed here, we'll set it
                    // CurriculumVersion cv = curriculumVersionRepository.findById(curriculumVersionId)...
                    // For now, I'll just save the plan.
                    return studyPlanRepository.save(newPlan);
                });
    }
}
