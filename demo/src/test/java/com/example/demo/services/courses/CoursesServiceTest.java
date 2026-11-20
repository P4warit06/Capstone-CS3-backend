package com.example.demo.services.courses;

import com.example.demo.entity.ReviewStatus;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoursesServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private CoursesService coursesService;

    @Test
    void combinesSearchCategoryAndRatingSortInSingleRepositoryQuery() {
        Pageable pageable = PageRequest.of(0, 24, Sort.by("courseCode"));
        when(courseRepository.findCourses(eq("data"), eq(true), eq(true), eq(List.of("GE")), eq("rating,desc"),
                eq(ReviewStatus.visible), any(Pageable.class)))
                .thenReturn(Page.empty(PageRequest.of(0, 24)));

        coursesService.getAllCourses(" data ", "ge", "rating,desc", pageable);

        verify(courseRepository).findCourses(eq("data"), eq(true), eq(true), eq(List.of("GE")), eq("rating,desc"),
                eq(ReviewStatus.visible), org.mockito.ArgumentMatchers.argThat(request ->
                        request.getPageNumber() == 0 && request.getPageSize() == 24 && request.getSort().isUnsorted()));
    }

    @Test
    void sendsEmptyQueryStringWhenSearchIsNotSpecified() {
        Pageable pageable = PageRequest.of(0, 24);
        when(courseRepository.findCourses(eq(""), eq(false), eq(false), eq(List.of("__NO_CATEGORY__")),
                eq("courseCode,asc"), eq(ReviewStatus.visible), any(Pageable.class)))
                .thenReturn(Page.empty(pageable));

        coursesService.getAllCourses(null, null, "courseCode,asc", pageable);

        verify(courseRepository).findCourses(eq(""), eq(false), eq(false), eq(List.of("__NO_CATEGORY__")),
                eq("courseCode,asc"), eq(ReviewStatus.visible), any(Pageable.class));
    }
}
