package com.example.demo.dtos;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class CurriculumSummaryDto {
    private String title;
    private Integer totalCredits;
    private List<CategorySummaryDto> categories;

    public CurriculumSummaryDto() {}

    public CurriculumSummaryDto(String title, Integer totalCredits, List<CategorySummaryDto> categories) {
        this.title = title;
        this.totalCredits = totalCredits;
        this.categories = categories;
    }
}
