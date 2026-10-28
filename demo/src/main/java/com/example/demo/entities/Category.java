package com.example.demo.entities;

import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    private String nameTh;
    private String nameEn;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<Course> courses;

    public Category() {}

    public Category(Long id, String code, String nameTh, String nameEn, List<Course> courses) {
        this.id = id;
        this.code = code;
        this.nameTh = nameTh;
        this.nameEn = nameEn;
        this.courses = courses;
    }
    public Long getId() {
        return id;
    }
    public String getCode() {
        return code;
    }
    public String getNameTh() {
        return nameTh;
    }
    public String getNameEn() {
        return nameEn;
    }
    public List<Course> getCourses() {
        return courses;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public void setNameTh(String nameTh) {
        this.nameTh = nameTh;
    }
    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }
    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }
}
