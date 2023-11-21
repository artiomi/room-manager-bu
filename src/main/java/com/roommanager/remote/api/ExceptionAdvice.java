package com.roommanager.remote.api;

import jakarta.validation.ConstraintViolationException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
@Slf4j
public class ExceptionAdvice {

  @ExceptionHandler({ConstraintViolationException.class, MethodArgumentTypeMismatchException.class})
  public ResponseEntity<Map<String, String>> parameterParsingException(Throwable cause) {
    log.error("parameter parsing failed.", cause);
    return new ResponseEntity<>(Map.of("message", cause.getMessage()), HttpStatus.BAD_REQUEST);
  }
}
