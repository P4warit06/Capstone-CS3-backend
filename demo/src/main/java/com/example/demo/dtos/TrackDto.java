package com.example.demo.dtos;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@SuppressWarnings("unused")
@Getter
@Setter
public class TrackDto {
    private Integer id;
    private String nameTh;
    private String nameEn;
    private String description;
    private TrackGroupDto group;

    public TrackDto() {}

    public TrackDto(Integer id, String nameTh, String nameEn, String description, TrackGroupDto group) {
        this.id = id;
        this.nameTh = nameTh;
        this.nameEn = nameEn;
        this.description = description;
        this.group = group;
    }
}
