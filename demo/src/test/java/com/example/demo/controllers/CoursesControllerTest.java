package com.example.demo.controllers;

import com.example.demo.dtos.CourseDto;
import com.example.demo.dtos.PagedResponseDto;
import com.example.demo.services.courses.CoursesService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoursesControllerTest {

    @Mock
    private CoursesService coursesService;

    @InjectMocks
    private CoursesController coursesController;

    @Test
    void rejectsMalformedSortWithBadRequest() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> coursesController.getAllCourses(0, 24, "abc", null, null));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void rejectsUnknownSortFieldWithBadRequest() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> coursesController.getAllCourses(0, 24, "foo,asc", null, null));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void passesUnsortedPageableAndRequestedFiltersToService() {
        when(coursesService.getAllCourses(isNull(), eq("GE"), eq("rating,desc"), any(Pageable.class)))
                .thenReturn(Page.empty(PageRequest.of(0, 24)));

        PagedResponseDto<CourseDto> response = coursesController.getAllCourses(0, 24, "rating,desc", null, "GE");

        assertEquals(0, response.getPage());
        verify(coursesService).getAllCourses(isNull(), eq("GE"), eq("rating,desc"),
                org.mockito.ArgumentMatchers.argThat(pageable -> pageable.getSort().isUnsorted()));
    }
}
