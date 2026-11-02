package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "track_courses")
public class TrackCourse {

    @EmbeddedId
    private TrackCourseId id = new TrackCourseId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("trackId")
    @JoinColumn(name = "track_id", nullable = false)
    private Track track;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("courseId")
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "is_required", nullable = false)
    private Boolean isRequired;

    @Column(name = "standard_year", nullable = false)
    private Integer standardYear;

    @Column(name = "standard_semester", nullable = false)
    private Integer standardSemester;

    @Column(name = "choice_group")
    private Integer choiceGroup;

    public TrackCourse() {}

    @Getter
    @Setter
    @Embeddable
    public static class TrackCourseId implements Serializable {
        private Integer trackId;
        private Integer courseId;

        public TrackCourseId() {}

        public TrackCourseId(Integer trackId, Integer courseId) {
            this.trackId = trackId;
            this.courseId = courseId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TrackCourseId that = (TrackCourseId) o;
            return Objects.equals(trackId, that.trackId) &&
                   Objects.equals(courseId, that.courseId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(trackId, courseId);
        }
    }
}
