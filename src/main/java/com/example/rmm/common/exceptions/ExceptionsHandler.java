package com.example.rmm.common.exceptions;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@ControllerAdvice
public class ExceptionsHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFoundException(final NotFoundException exception) {
        final var body = Map.of("errors", List.of(exception.getMessage()));
        log.error(exception.getMessage(), exception.getCause());
        return ResponseEntity.status(NOT_FOUND).body(body);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, InvalidFormatException.class})
    public ResponseEntity<?> handleMethodArgumentTypeMismatchException(final MethodArgumentTypeMismatchException exception) {
        final var body = Map.of("errors", List.of("Unable to parse request body, property or parameter data type"));
        log.error(exception.getMessage(), exception.getCause());
        return ResponseEntity.status(BAD_REQUEST).body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrityViolationException(final DataIntegrityViolationException exception) {
        final var body = Map.of("errors", List.of("Provided data is violating a data integrity constraint"));
        log.error(exception.getMessage(), exception.getCause());
        return ResponseEntity.status(CONFLICT).body(body);
    }

    /**
     * This method does handle all those exceptions that are not
     * already handled in {@link ResponseEntityExceptionHandler}.
     *
     * @param exception to be handled.
     * @return ResponseEntity with status code and no body.
     * @see ResponseEntityExceptionHandler#handleException(Exception, WebRequest)
     */
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<?> handleException(final Exception exception) {
        return newNoBodyResponse(INTERNAL_SERVER_ERROR, exception);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            final HttpMessageNotReadableException exception,
            final HttpHeaders headers,
            final HttpStatusCode status,
            final WebRequest request) {
        // TODO create a method that returns the map and reuse
        final var body = Map.of("errors", List.of("Unable to parse request body or property data type"));
        log.error(exception.getMessage(), exception.getCause());
        return ResponseEntity.status(BAD_REQUEST).body(body);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            final Exception ex,
            final Object body,
            final HttpHeaders headers,
            final HttpStatusCode status,
            final WebRequest request
    ) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity.status(status).headers(headers).build();
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            final MethodArgumentNotValidException exception,
            final HttpHeaders headers,
            final HttpStatusCode status,
            final WebRequest request) {
        final var errors = exception.getBindingResult()
                                    .getFieldErrors()
                                    .stream()
                                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                                    .toList();
        log.error(exception.getMessage(), exception);
        return ResponseEntity.status(BAD_REQUEST).body(Map.of("errors", errors));
    }

    @Override
    protected ResponseEntity<Object> createResponseEntity(
            final Object body,
            final HttpHeaders headers,
            final HttpStatusCode status,
            final WebRequest request
    ) {
        return ResponseEntity.status(status).headers(headers).build();
    }

    private <T> ResponseEntity<T> newNoBodyResponse(
            final HttpStatus status,
            final Exception exception
    ) {
        log.error(exception.getMessage(), exception);
        return ResponseEntity.status(status).build();
    }
}
