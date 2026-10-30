package com.example.demo.entities;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "course_tags")
public class CourseTag {

    @EmbeddedId
    private CourseTagId id = new CourseTagId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("courseId")
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tagId")
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    public CourseTag() {}

    @Getter
    @Setter
    @Embeddable
    public static class CourseTagId implements Serializable {
        private Integer courseId;
        private Integer tagId;

        public CourseTagId() {}

        public CourseTagId(Integer courseId, Integer tagId) {
            this.courseId = courseId;
            this.tagId = tagId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CourseTagId that = (CourseTagId) o;
            return Objects.equals(courseId, that.courseId) &&
                   Objects.equals(tagId, that.tagId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(courseId, tagId);
        }
    }
}