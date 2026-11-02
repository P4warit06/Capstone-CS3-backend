package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "related_tracks")
@IdClass(RelatedTrackId.class)
public class RelatedTrack {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "track_id", nullable = false)
    private Track track;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_track_id", nullable = false)
    private Track relatedTrack;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    public RelatedTrack() {}
}