DO $$
BEGIN
    IF EXISTS (
        SELECT enrollment_id
        FROM student_course_grades
        GROUP BY enrollment_id
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION
            'Duplicate student_course_grades rows found; resolve them before applying the unique index';
    END IF;
END;
$$;

CREATE UNIQUE INDEX IF NOT EXISTS uk_student_course_grades_enrollment_id
    ON student_course_grades (enrollment_id);
