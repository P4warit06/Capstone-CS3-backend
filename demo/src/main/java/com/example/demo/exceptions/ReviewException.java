package com.example.demo.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/** error ของฟีเจอร์รีวิว — ส่ง code ให้ FE เลือกข้อความได้ (NOT_ENROLLED, ALREADY_REVIEWED, INVALID_REVIEW) */
@Getter
public class ReviewException extends RuntimeException {

    public static final String NOT_ENROLLED = "NOT_ENROLLED";
    public static final String ALREADY_REVIEWED = "ALREADY_REVIEWED";
    public static final String INVALID_REVIEW = "INVALID_REVIEW";

    private final HttpStatus status;
    private final String code;

    public ReviewException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }
}
