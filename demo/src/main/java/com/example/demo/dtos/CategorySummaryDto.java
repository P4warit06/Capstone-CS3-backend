package com.example.demo.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategorySummaryDto {
    private String key;
    private String label;
    private Integer credits;

    public CategorySummaryDto() {}

    public CategorySummaryDto(String key, String label, Integer credits) {
        this.key = key;
        this.label = label;
        this.credits = credits;
    }
}
