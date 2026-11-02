package com.example.demo.dtos;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class TrackDto {
    private Integer id;
    private String code;
    private String nameTh;
    private String nameEn;
    private String description;
    private Boolean isUserTrack;
    private TrackGroupDto group;

    public TrackDto() {}

    public TrackDto(Integer id, String code, String nameTh, String nameEn, String description, Boolean isUserTrack, TrackGroupDto group) {
        this.id = id;
        this.code = code;
        this.nameTh = nameTh;
        this.nameEn = nameEn;
        this.description = description;
        this.isUserTrack = isUserTrack;
        this.group = group;
    }
}
