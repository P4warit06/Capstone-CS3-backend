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

    @Transactional(readOnly = true)
    public List<TrackDto> getAllTracks(Integer userId) {
        List<Track> tracks = trackRepository.findAll();
        return tracks.stream().map(track -> {
            Boolean isUserTrack = false;
            if (userId != null) {
                isUserTrack = studyPlanRepository.findByUserIdAndTrackTrackId(userId, track.getTrackId()).isPresent();
            }

            String[] parsedNames = parseTrackName(track.getTrackName());

            return new TrackDto(
                    track.getTrackId(),
                    "", // Default to empty string instead of null
                    parsedNames[0],
                    parsedNames[1],
                    track.getDescription(),
                    isUserTrack,
                    new TrackGroupDto(0, "ทั่วไป", "General") // Default group instead of null
            );
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TrackDetailDto getTrackById(Integer id, Integer userId) {
        Track track = trackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Track not found"));

        TrackDetailDto detail = new TrackDetailDto();
        detail.setId(track.getTrackId());
        detail.setCode(""); // Default to empty string instead of null

        String[] parsedNames = parseTrackName(track.getTrackName());
        detail.setNameTh(parsedNames[0]);
        detail.setNameEn(parsedNames[1]);
        detail.setDescription(track.getDescription());

        Boolean isUserTrack = false;
        if (userId != null) {
            isUserTrack = studyPlanRepository.findByUserIdAndTrackTrackId(userId, track.getTrackId()).isPresent();
        }
        detail.setIsUserTrack(isUserTrack);
        detail.setTotalCredits(track.getRequiredCredits());

        detail.setGroup(new TrackGroupDto(0, "ทั่วไป", "General")); // Default group instead of null

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
                                return new TrackCourseDto(
                                        course.getCourseId(),
                                        course.getCourseCode(),
                                        course.getNameTh(),
                                        course.getNameEn(),
                                        course.getCredits(),
                                        tc.getIsRequired(),
                                        rating
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
                .map(cc -> {
                    Category cat = cc.getCategory();
                    return new CategorySummaryDto(cat.getCode(), cat.getCategoryNameTh(), cc.getRequiredCredits());
                }).collect(Collectors.toList());

        detail.setCurriculumSummary(new CurriculumSummaryDto(cv.getVersionName(), cv.getTotalCredits(), catSummaries));

        // Related Tracks
        List<RelatedTrackDto> related = trackRepository.findAll().stream()
                .filter(t -> !t.getTrackId().equals(id) && t.getCurriculumVersion().equals(cv))
                .map(t -> {
                    String[] parsed = parseTrackName(t.getTrackName());
                    return new RelatedTrackDto(t.getTrackId(), "", parsed[0], parsed[1]);
                })
                .collect(Collectors.toList());
        detail.setRelatedTracks(related);

        return detail;
    }

    private String[] parseTrackName(String trackName) {
        if (trackName == null) return new String[]{"", ""};
        // Remove leading "N " (e.g., "1 นักพัฒนาฟรอนต์เอนด์ (Frontend Developer)")
        String cleaned = trackName.replaceFirst("^\\d+\\s+", "");
        // Split by " ("
        int bracketIndex = cleaned.indexOf(" (");
        if (bracketIndex != -1) {
            String nameTh = cleaned.substring(0, bracketIndex);
            String nameEn = cleaned.substring(bracketIndex + 2, cleaned.length() - 1);
            return new String[]{nameTh, nameEn};
        }
        return new String[]{cleaned, ""};
    }
}