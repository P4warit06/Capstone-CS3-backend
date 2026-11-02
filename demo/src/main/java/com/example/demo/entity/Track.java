package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tracks")
public class Track {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "track_id")
    private Integer trackId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curriculum_version_id", nullable = false)
    private CurriculumVersion curriculumVersion;

    @Column(name = "track_name", nullable = false, length = 100)
    private String trackName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "required_credits", nullable = false)
    private Integer requiredCredits;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // *** ให้ลบฟิลด์ @ManyToOne ของ TrackGroup ออกจากไฟล์นี้ ***

    public Track() {}
}