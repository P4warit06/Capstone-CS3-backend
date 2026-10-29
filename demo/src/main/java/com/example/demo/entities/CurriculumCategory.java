package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "curriculum_categories")
public class CurriculumCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "curriculum_category_id")
    private Integer curriculumCategoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "required_credits", nullable = false)
    private Integer requiredCredits;

    // ... ฟิลด์อื่น เช่น curriculumVersion, parentCurriculumCategory
}