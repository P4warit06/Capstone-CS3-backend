package com.example.demo.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RelatedTrackDto {
    private Integer id;
    private String code;
    private String nameTh;

    public RelatedTrackDto() {}

    public RelatedTrackDto(Integer id, String code, String nameTh) {
        this.id = id;
        this.code = code;
        this.nameTh = nameTh;
    }
}
