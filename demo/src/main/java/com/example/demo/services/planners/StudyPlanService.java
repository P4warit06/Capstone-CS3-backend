package com.example.demo.services.planners;

import com.example.demo.dtos.CreatePlanRequestDto;
import com.example.demo.dtos.PlanDto;
import com.example.demo.dtos.PlanListResponseDto;
import com.example.demo.dtos.PlanTrackDto;
import com.example.demo.entity.RegistrarStudent;
import com.example.demo.entity.StudyPlan;
import com.example.demo.entity.User;
import com.example.demo.exceptions.PlanLimitExceededException;
import com.example.demo.repository.RegistrarStudentRepository;
import com.example.demo.repository.StudyPlanRepository;
import com.example.demo.repository.TrackRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudyPlanService {

    private final StudyPlanRepository studyPlanRepository;
    private final RegistrarStudentRepository registrarStudentRepository;
    private final UserRepository userRepository;
    private final TrackRepository trackRepository;

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