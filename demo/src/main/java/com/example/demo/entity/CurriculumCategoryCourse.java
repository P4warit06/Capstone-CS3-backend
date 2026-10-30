package com.example.demo.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "curriculum_category_courses")

public class CurriculumCategoryCourse {

    @EmbeddedId
    private CurriculumCategoryCourseId id = new CurriculumCategoryCourseId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("courseId")
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("curriculumCategoryId")
    @JoinColumn(name = "curriculum_category_id", nullable = false)
    private CurriculumCategory curriculumCategory;

    @Column(name = "is_required", nullable = false)
    private Boolean isRequired = false;

    @Column(name = "standard_year")
    private Integer standardYear;

    @Column(name = "standard_semester")
    private Integer standardSemester;

    // คงฟิลด์เดิมไว้ตามเงื่อนไข (ปิด insert/update เพื่อไม่ให้ซ้ำซ้อนกับคอลัมน์ is_required)
    @Column(name = "is_required", nullable = false, insertable = false, updatable = false)
    private Boolean isActive = true;

    public CurriculumCategoryCourse() {}

    @Getter
    @Setter
    @Embeddable
    public static class CurriculumCategoryCourseId implements Serializable {
        private Integer curriculumCategoryId;
        private Integer courseId;

        public CurriculumCategoryCourseId() {}

        public CurriculumCategoryCourseId(Integer curriculumCategoryId, Integer courseId) {
            this.curriculumCategoryId = curriculumCategoryId;
            this.courseId = courseId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CurriculumCategoryCourseId that = (CurriculumCategoryCourseId) o;
            return Objects.equals(curriculumCategoryId, that.curriculumCategoryId) &&
                   Objects.equals(courseId, that.courseId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(curriculumCategoryId, courseId);
        }
    }

    public Category getCategory() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCategory'");
    }
}