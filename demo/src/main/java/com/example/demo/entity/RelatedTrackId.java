package com.example.demo.entity;

import java.io.Serializable;
import java.util.Objects;

public class RelatedTrackId implements Serializable {

    private Integer track;
    private Integer relatedTrack;

    public RelatedTrackId() {}

    public RelatedTrackId(Integer track, Integer relatedTrack) {
        this.track = track;
        this.relatedTrack = relatedTrack;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RelatedTrackId that = (RelatedTrackId) o;
        return Objects.equals(track, that.track) && Objects.equals(relatedTrack, that.relatedTrack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(track, relatedTrack);
    }
}