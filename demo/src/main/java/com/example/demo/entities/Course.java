package com.example.demo.entities;
import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String courseCode;
    private String nameTh;
    private String nameEn;
    private Integer credits;
    private Boolean isActive;

    @ElementCollection
    private List<String> tags;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    public Course() {}

    public Course(Long id, String courseCode, String nameTh, String nameEn, Integer credits, Boolean isActive, List<String> tags, Category category) {
        this.id = id;
        this.courseCode = courseCode;
        this.nameTh = nameTh;
        this.nameEn = nameEn;
        this.credits = credits;
        this.isActive = isActive;
        this.tags = tags;
        this.category = category;
    }
    public Long getId() {
        return id;
    }
    public String getCourseCode() {
        return courseCode;
    }
    public String getNameTh() {
        return nameTh;
    }
    public String getNameEn() {
        return nameEn;
    }
    public Integer getCredits() {
        return credits;
    }
    public Boolean getIsActive() {
        return isActive;
    }
    public List<String> getTags() {
        return tags;
    }
    public Category getCategory() {
        return category;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }
    public void setNameTh(String nameTh) {
        this.nameTh = nameTh;
    }
    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }
    public void setCredits(Integer credits) {
        this.credits = credits;
    }
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    public void setCategory(Category category) {
        this.category = category;
    }
}
