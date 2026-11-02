package com.example.demo.repository;

import com.example.demo.entity.StudyPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StudyPlanRepository extends JpaRepository<StudyPlan, Integer> {
    Optional<StudyPlan> findByUserId(Integer userId);
    Optional<StudyPlan> findByUserIdAndTrackTrackIdAndIsDefaultTrue(Integer userId, Integer trackId);
    Optional<StudyPlan> findByUserIdAndTrackTrackId(Integer userId, Integer trackId);
}
