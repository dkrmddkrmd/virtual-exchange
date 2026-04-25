package com.example.virtual_exchange.exception;

import com.example.virtual_exchange.dto.ErrorResponseDto;
import com.example.virtual_exchange.service.ErrorLogService;
import com.example.virtual_exchange.service.detection.SlackNotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final SlackNotificationService slackNotificationService;
    private final ErrorLogService errorLogService;

    @ExceptionHandler(AbnormalTradeException.class)
    public ResponseEntity<ErrorResponseDto> handelAbnormalTrade(AbnormalTradeException ex, HttpServletRequest request) {
        log.error("비정상적인 거래 발생: {}", ex.getMessage());
        errorLogService.saveErrorLog(ex, request);
        ErrorCode errorCode = ErrorCode.ABNORMAL_TRADE;

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponseDto.of(errorCode, ex.getMessage()));
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponseDto> handleDuplicateEmail(DuplicateEmailException ex, HttpServletRequest request) {
        log.error("중복 가입 시도 감지: {}", ex.getMessage(), ex);
        errorLogService.saveErrorLog(ex, request);
        ErrorCode errorCode = ErrorCode.DUPLICATE_EMAIL;

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponseDto.of(errorCode, errorCode.getMessage()));
    }

    @ExceptionHandler(UpbitApiCallException.class)
    public ResponseEntity<ErrorResponseDto> handleApiCallError(UpbitApiCallException ex, HttpServletRequest request) {
        log.error("Upbit API 오류: {}", ex.getMessage(), ex);
        slackNotificationService.sendServerError("UpbitApiCallException", ex.getMessage());
        errorLogService.saveErrorLog(ex, request);
        ErrorCode errorCode = ErrorCode.UPBIT_API_ERROR;

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponseDto.of(errorCode, errorCode.getMessage()));
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidRefreshToken(InvalidRefreshTokenException ex, HttpServletRequest request) {
        log.warn("유효하지 않은 리프레시 토큰 감지: {}", ex.getMessage());
        errorLogService.saveErrorLog(ex, request);
        ErrorCode errorCode = ErrorCode.INVALID_REFRESH_TOKEN;

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponseDto.of(errorCode));
    }

    @ExceptionHandler(KafkaProducerErrorException.class)
    public ResponseEntity<ErrorResponseDto> handlerKafkaError(KafkaProducerErrorException ex, HttpServletRequest request) {
        log.error("카프카 API 요청 오류: {}", ex.getMessage(), ex);
        slackNotificationService.sendServerError("KafkaProducerErrorException", ex.getMessage());
        errorLogService.saveErrorLog(ex, request);
        ErrorCode errorCode = ErrorCode.Kafka_API_ERROR;

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponseDto.of(errorCode, errorCode.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.error("유효성 검사 실패: {}", message);
        errorLogService.saveErrorLog(ex, request);
        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponseDto.of(errorCode, message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        errorLogService.saveErrorLog(ex, request);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto("BAD_REQUEST", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        errorLogService.saveErrorLog(ex, request);

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponseDto("DUPLICATE_RESOURCE", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception", ex);
        slackNotificationService.sendServerError(ex.getClass().getSimpleName(), ex.getMessage());
        errorLogService.saveErrorLog(ex, request);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDto("SERVER_ERROR", "서버 내부 오류가 발생했습니다. 관리자에게 문의하세요."));
    }
}
