package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id") // ตรงกับ PK ใน SQL
    private Integer categoryId;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "category_name_th", nullable = false, length = 100)
    private String categoryNameTh;

    @Column(name = "category_name_en", nullable = false, length = 100)
    private String categoryNameEn;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Category() {}
}