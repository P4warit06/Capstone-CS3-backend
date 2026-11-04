package com.example.demo.repository;

import com.example.demo.entity.TrackCourse;
import com.example.demo.entity.TrackCourse.TrackCourseId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TrackCourseRepository extends JpaRepository<TrackCourse, TrackCourseId> {
    List<TrackCourse> findByTrackTrackId(Integer trackId);
    List<TrackCourse> findByTrackTrackIdAndIsRequiredTrue(Integer trackId);
}
