package com.example.demo.repository;

import com.example.demo.entity.RelatedTrack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RelatedTrackRepository extends JpaRepository<RelatedTrack, Integer> {
    List<RelatedTrack> findByTrackTrackId(Integer trackId);
}
