package com.airline.auth.controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.*;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.*;
@RestControllerAdvice
public class ApiErrors {
  @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<Map<String,Object>> invalid(MethodArgumentNotValidException ex){
    return ResponseEntity.badRequest().body(Map.of("status",400,"message",ex.getBindingResult().getFieldErrors().stream().map(e->e.getField()+": "+e.getDefaultMessage()).toList()));
  }
  @ExceptionHandler(ResponseStatusException.class) ResponseEntity<Map<String,Object>> error(ResponseStatusException ex) {
    return ResponseEntity.status(ex.getStatusCode()).body(Map.of("status",ex.getStatusCode().value(),"message",ex.getReason()==null?"Request failed":ex.getReason()));
  }
  @ExceptionHandler(DataIntegrityViolationException.class) ResponseEntity<?> duplicate(DataIntegrityViolationException ex){
    return ResponseEntity.status(409).body(Map.of("status",409,"message","Email already registered"));
  }
}
