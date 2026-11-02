package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime; 
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "track_groups")
public class TrackGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "track_group_id")
    private Integer trackGroupId;

    @Column(name = "group_name_th", nullable = false, length = 100)
    private String nameTh;

    @Column(name = "group_name_en", nullable = false, length = 100)
    private String nameEn;

    @Column(name = "sort_order", columnDefinition = "TEXT")
    private Integer SortOrder;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "group")
    private List<Track> tracks;

    public TrackGroup() {}
}
