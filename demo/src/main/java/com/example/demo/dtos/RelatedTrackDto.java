package com.example.demo.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RelatedTrackDto {
    private Integer id;
    private String code;
    private String nameTh;
    private String nameEn;

    public RelatedTrackDto() {}

    public RelatedTrackDto(Integer id, String code, String nameTh, String nameEn) {
        this.id = id;
        this.code = code;
        this.nameTh = nameTh;
        this.nameEn = nameEn;
    }
}
