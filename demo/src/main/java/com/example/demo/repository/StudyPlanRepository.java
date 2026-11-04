package com.example.demo.repository;

import com.example.demo.entity.StudyPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyPlanRepository extends JpaRepository<StudyPlan, Integer> {
    Optional<StudyPlan> findByUserId(Integer userId);
    Optional<StudyPlan> findByUserIdAndTrackTrackIdAndIsDefaultTrue(Integer userId, Integer trackId);
    Optional<StudyPlan> findByUserIdAndTrackTrackId(Integer userId, Integer trackId);

    // ค้นหาตาม userId โดยเรียง isDefault DESC (แผนหลักขึ้นก่อน) แล้วตามด้วย createdAt ASC (จากเก่าไปใหม่)
    @Query("SELECT sp FROM StudyPlan sp LEFT JOIN FETCH sp.track WHERE sp.userId = :userId ORDER BY sp.isDefault DESC, sp.createdAt ASC")
    List<StudyPlan> findByUserIdCustomOrder(@Param("userId") Integer userId);

    long countByUserId(Integer userId);
}
