package com.srm.creditengine.api.error;

import com.srm.creditengine.domain.shared.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private final Clock clock;

  public GlobalExceptionHandler(Clock clock) {
    this.clock = clock;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
      MethodArgumentNotValidException exception, HttpServletRequest request) {
    return buildValidationResponse(
        HttpStatus.BAD_REQUEST,
        "Validation failed.",
        exception.getBindingResult().getFieldErrors(),
        request);
  }

  @ExceptionHandler(BindException.class)
  public ResponseEntity<ApiErrorResponse> handleBindException(
      BindException exception, HttpServletRequest request) {
    return buildValidationResponse(
        HttpStatus.BAD_REQUEST,
        "Validation failed.",
        exception.getBindingResult().getFieldErrors(),
        request);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
      ConstraintViolationException exception, HttpServletRequest request) {
    List<ApiErrorDetail> details =
        exception.getConstraintViolations().stream()
            .map(
                violation ->
                    new ApiErrorDetail(
                        violation.getPropertyPath().toString(), violation.getMessage()))
            .toList();

    return buildResponse(
        HttpStatus.BAD_REQUEST, "Validation failed.", request.getRequestURI(), details);
  }

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiErrorResponse> handleBusinessException(
      BusinessException exception, HttpServletRequest request) {
    return buildResponse(
        exception.status(), exception.getMessage(), request.getRequestURI(), exception.details());
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ApiErrorResponse> handleMissingServletRequestParameter(
      MissingServletRequestParameterException exception, HttpServletRequest request) {
    return buildResponse(
        HttpStatus.BAD_REQUEST,
        "Validation failed.",
        request.getRequestURI(),
        List.of(new ApiErrorDetail(exception.getParameterName(), exception.getMessage())));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
      Exception exception, HttpServletRequest request) {
    return buildResponse(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "An unexpected error occurred.",
        request.getRequestURI(),
        List.of());
  }

  private ResponseEntity<ApiErrorResponse> buildValidationResponse(
      HttpStatus status, String message, List<FieldError> fieldErrors, HttpServletRequest request) {
    List<ApiErrorDetail> details =
        fieldErrors.stream()
            .map(error -> new ApiErrorDetail(error.getField(), error.getDefaultMessage()))
            .toList();

    return buildResponse(status, message, request.getRequestURI(), details);
  }

  private ResponseEntity<ApiErrorResponse> buildResponse(
      HttpStatus status, String message, String path, List<ApiErrorDetail> details) {
    ApiErrorResponse response =
        new ApiErrorResponse(
            Instant.now(clock), status.value(), status.getReasonPhrase(), message, path, details);
    return ResponseEntity.status(status).body(response);
  }
}
