package com.example.demo.repository;

import com.example.demo.entity.SavedCourse;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RepositoryRestResource(exported = false)
public interface SavedCourseRepository extends JpaRepository<SavedCourse, Integer> {

    @EntityGraph(attributePaths = "course")
    List<SavedCourse> findByUser_IdOrderByCreatedAtDescSavedCourseIdDesc(Integer userId);

    boolean existsByUser_IdAndCourse_CourseId(Integer userId, Integer courseId);

    Optional<SavedCourse> findByUser_IdAndCourse_CourseId(Integer userId, Integer courseId);
}
