package com.airline.flight.controller;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;
@RestControllerAdvice
public class ApiErrors {
  @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<?> invalid(MethodArgumentNotValidException e){return ResponseEntity.badRequest().body(Map.of("status",400,"message",e.getBindingResult().getFieldErrors().stream().map(f->f.getField()+": "+f.getDefaultMessage()).toList()));}
  @ExceptionHandler(ResponseStatusException.class) ResponseEntity<?> status(ResponseStatusException e){return ResponseEntity.status(e.getStatusCode()).body(Map.of("status",e.getStatusCode().value(),"message",e.getReason()==null?"Invalid request":e.getReason()));}
  @ExceptionHandler(DataIntegrityViolationException.class) ResponseEntity<?> conflict(DataIntegrityViolationException e){return ResponseEntity.status(409).body(Map.of("status",409,"message","Flight already exists"));}
}
