package com.example.virtual_exchange.exception;

import com.example.virtual_exchange.dto.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponseDto> handleDuplicateEmail(DuplicateEmailException ex) {
        // log.error("중복 가입 시도 감지: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponseDto("DUPLICATE_EMAIL", ex.getMessage()));
    }

    // 1. @Valid 검증 실패 시 (예: 이메일 형식이 아님, 수량이 0 이하)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // 첫 번째 에러 메시지만 뽑아서 보냄
        String errorMessage = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto("INVALID_INPUT", errorMessage));
    }

    // 2. 비즈니스 로직 에러 (예: throw new IllegalArgumentException("잔액 부족"))
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto("BAD_REQUEST", ex.getMessage()));
    }

    // 3. 잘못된 상태 (중복 가입 등) -> 409 Conflict 또는 400 Bad Request
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT) // 409 Conflict (충돌)
                .body(new ErrorResponseDto("DUPLICATE_RESOURCE", ex.getMessage()));
    }

    // 4. 그 외 예상치 못한 모든 에러 (최후의 보루)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleException(Exception ex) {
        ex.printStackTrace(); // 콘솔에 에러 로그는 찍어줌 (디버깅용)

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDto("SERVER_ERROR", "서버 내부 오류가 발생했습니다. 관리자에게 문의하세요."));
    }
}