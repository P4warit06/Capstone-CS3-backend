package com.example.demo.repository;

import com.example.demo.entity.Course;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(exported = false)
public interface CoursePrerequisiteRepository extends Repository<Course, Integer> {

    @Query(value = """
            SELECT cp.course_id AS "courseId",
                   cp.prerequisite_course_id AS "prerequisiteCourseId",
                   cp.group_no AS "groupNo",
                   cp.is_corequisite AS "corequisite",
                   prerequisite.course_code AS "prerequisiteCourseCode",
                   prerequisite.name_th AS "prerequisiteCourseNameTh",
                   prerequisite.name_en AS "prerequisiteCourseNameEn"
            FROM course_prerequisites cp
            JOIN courses prerequisite ON prerequisite.course_id = cp.prerequisite_course_id
            WHERE cp.course_id IN (:courseIds)
            ORDER BY cp.course_id, cp.group_no, prerequisite.course_code
            """, nativeQuery = true)
    List<CoursePrerequisiteView> findByCourseIds(@Param("courseIds") List<Integer> courseIds);

    interface CoursePrerequisiteView {
        Integer getCourseId();

        Integer getPrerequisiteCourseId();

        Integer getGroupNo();

        Boolean getCorequisite();

        String getPrerequisiteCourseCode();

        String getPrerequisiteCourseNameTh();

        String getPrerequisiteCourseNameEn();
    }
}
