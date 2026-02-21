package com.example.backend.exception;

import com.example.backend.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DataIntegrityViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse> handleRuntime(RuntimeException ex) {

        // For “already exists” we should return 409 Conflict (not 500)
        String msg = ex.getMessage() == null ? "Request failed" : ex.getMessage();

        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (msg.toLowerCase().contains("already exists")) {
            status = HttpStatus.CONFLICT; // 409
        }

        return ResponseEntity.status(status).body(
                new ApiResponse(false, msg,null)
        );
    }


    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse> handleIntegrity(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponse(false, "Duplicate value detected (email / business reg no / NMRA).",null));
    }

}
