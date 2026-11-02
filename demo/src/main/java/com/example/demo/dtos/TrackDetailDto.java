package com.example.demo.dtos;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class TrackDetailDto {
    private Integer id;
    private String code;
    private String nameTh;
    private String nameEn;
    private String description;
    private Boolean isUserTrack;
    private Integer totalCredits;
    private TrackGroupDto group;
    private List<SemesterDto> semesters;
    private CurriculumSummaryDto curriculumSummary;
    private List<RelatedTrackDto> relatedTracks;

    public TrackDetailDto() {}
}
