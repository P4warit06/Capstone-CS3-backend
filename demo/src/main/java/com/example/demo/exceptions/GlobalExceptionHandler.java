package com.example.demo.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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

    // SCRUM-203: {"code": "ALREADY_REVIEWED", "message": "..."}
    @ExceptionHandler(ReviewException.class)
    public ResponseEntity<Map<String, String>> handleReviewException(ReviewException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(Map.of("code", ex.getCode(), "message", ex.getMessage()));
    }

    // SCRUM-242: ข้อมูลชน constraint ใน DB — log รายละเอียดไว้ฝั่ง server ไม่ส่ง SQL error กลับไปให้ FE
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.error("Data integrity violation", ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", "บันทึกข้อมูลไม่สำเร็จ กรุณาลองใหม่อีกครั้ง"));
    }
}
