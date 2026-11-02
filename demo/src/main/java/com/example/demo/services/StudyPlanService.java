package com.example.demo.services;

import com.example.demo.entity.StudyPlan;
import com.example.demo.repository.StudyPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class StudyPlanService {

    @Autowired
    private StudyPlanRepository studyPlanRepository;

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
