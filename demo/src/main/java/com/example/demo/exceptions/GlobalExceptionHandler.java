package com.example.demo.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CourseNotFoundException.class)
    public ResponseEntity<Map> handleCourseNotFound(CourseNotFoundException ex) {
        Map<String, Object> errorResponse = Map.of(
                "code", 404,
                "message", "Not Found",
                "details", ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(PlanLimitExceededException.class)
    public ResponseEntity<Map<String, String>> handlePlanLimitExceeded(PlanLimitExceededException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", ex.getMessage()));
    }

    // ส่ง reason ของ ResponseStatusException กลับเป็น {"message": ...} ให้ FE เอาไปแสดงได้
    // (ค่าเริ่มต้นของ Spring ไม่ส่งข้อความกลับไป)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatus(ResponseStatusException ex) {
        String message = ex.getReason() != null ? ex.getReason() : ex.getStatusCode().toString();
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("message", message));
    }
}
