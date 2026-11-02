package com.example.demo.services.tracks;

import com.example.demo.dtos.*;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TracksService {

    @Autowired
    private TrackRepository trackRepository;

    @Autowired
    private TrackCourseRepository trackCourseRepository;

    @Autowired
    private StudyPlanRepository studyPlanRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private CurriculumCategoryRepository curriculumCategoryRepository;

    @Autowired
    private RelatedTrackRepository relatedTrackRepository;

    

    @Transactional(readOnly = true)
    public List<TrackDto> getAllTracks(Integer userId) {
        List<Track> tracks = trackRepository.findAll();
        return tracks.stream().map(track -> {
            TrackGroup group = track.getGroup();
            TrackGroupDto groupDto = group != null ? new TrackGroupDto(group.getTrackGroupId(), group.getNameTh(), group.getNameEn()) : null;

            return new TrackDto(
                    track.getTrackId(),
                    track.getTrackNameTh(),
                    track.getTrackNameEn(),
                    track.getDescriptionTh(),
                    groupDto
            );
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TrackDetailDto getTrackById(Integer id, Integer userId) {
        Track track = trackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Track not found"));

        TrackDetailDto detail = new TrackDetailDto();
        detail.setId(track.getTrackId());
        detail.setNameTh(track.getTrackNameTh());
        detail.setNameEn(track.getTrackNameEn());
        detail.setDescription(track.getDescriptionTh());

        detail.setTotalCredits(track.getRequiredCredits());

        TrackGroup group = track.getGroup();
        detail.setGroup(group != null ? new TrackGroupDto(group.getTrackGroupId(), group.getNameTh(), group.getNameEn()) : null);

        // Semesters & Courses
        List<TrackCourse> trackCourses = trackCourseRepository.findByTrackTrackId(track.getTrackId());
        Map<String, List<TrackCourse>> grouped = trackCourses.stream()
                .collect(Collectors.groupingBy(tc -> tc.getStandardYear() + "-" + tc.getStandardSemester()));

        List<SemesterDto> semesters = grouped.entrySet().stream()
                .map(entry -> {
                    String[] parts = entry.getKey().split("-");
                    Integer year = Integer.parseInt(parts[0]);
                    Integer semester = Integer.parseInt(parts[1]);
                    List<TrackCourse> coursesInSemester = entry.getValue();

                    int totalCredits = coursesInSemester.stream()
                            .mapToInt(tc -> tc.getCourse().getCredits())
                            .sum();

                    List<TrackCourseDto> courseDtos = coursesInSemester.stream()
                            .map(tc -> {
                                Course course = tc.getCourse();
                                Double rating = reviewRepository.getAverageRatingByCourseId(course.getCourseId());
                                
                                // แปลง Integer เป็น String ก่อนส่งให้ TrackCourseDto
                                String choiceGroupStr = tc.getChoiceGroup() != null ? String.valueOf(tc.getChoiceGroup()) : null;

                                return new TrackCourseDto(
                                        course.getCourseId(),
                                        course.getCourseCode(),
                                        course.getNameTh(),
                                        course.getNameEn(),
                                        course.getCredits(),
                                        tc.getIsRequired(),
                                        rating,
                                        tc.getChoiceGroup()
                                );
                            }).collect(Collectors.toList());

                    return new SemesterDto(year, semester, totalCredits, courseDtos);
                })
                .sorted(Comparator.comparingInt(SemesterDto::getYear)
                        .thenComparingInt(SemesterDto::getSemester))
                .collect(Collectors.toList());

        detail.setSemesters(semesters);

        // Curriculum Summary
        CurriculumVersion cv = track.getCurriculumVersion();
        List<CategorySummaryDto> catSummaries = curriculumCategoryRepository.findByCurriculumVersionCurriculumVersionId(cv.getCurriculumVersionId())
                .stream()
                .sorted(Comparator.comparing(CurriculumCategory::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(cc -> {
                    Category cat = cc.getCategory();
                    return new CategorySummaryDto(cat.getCode(), cat.getCategoryNameTh(), cc.getRequiredCredits());
                }).collect(Collectors.toList());

        detail.setCurriculumSummary(new CurriculumSummaryDto(cv.getVersionName(), cv.getTotalCredits(), catSummaries));

        // Related Tracks
        List<RelatedTrack> relatedEntities = relatedTrackRepository.findByTrackTrackId(track.getTrackId());
        List<RelatedTrackDto> related = relatedEntities.stream()
                .map(rt -> {
                    Track rtTrack = rt.getRelatedTrack();
                    return new RelatedTrackDto(rtTrack.getTrackId(), "", rtTrack.getTrackNameTh(), rtTrack.getTrackNameEn());
                })
                .collect(Collectors.toList());
        detail.setRelatedTracks(related);

        return detail;
    }
}