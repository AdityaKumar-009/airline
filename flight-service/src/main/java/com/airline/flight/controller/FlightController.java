package com.airline.flight.controller;
import com.airline.flight.dto.FlightDtos.*;
import com.airline.flight.service.FlightService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
@RestController @RequestMapping("/api/flights")
public class FlightController {
  private final FlightService service;
  public FlightController(FlightService s){service=s;}
  @GetMapping public List<FlightResponse> search(@RequestParam(required=false)String origin,@RequestParam(required=false)String destination,
    @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate date){return service.search(origin,destination,date);}
  @GetMapping("/manage") @SecurityRequirement(name="bearerAuth") public List<FlightResponse> manage(){return service.manage();}
  @GetMapping("/{id}") public FlightResponse get(@PathVariable Long id){return service.get(id);}
  @PostMapping @ResponseStatus(HttpStatus.CREATED) @SecurityRequirement(name="bearerAuth") public FlightResponse create(@Valid @RequestBody CreateFlight r){return service.create(r);}
  @PatchMapping("/{id}") @SecurityRequirement(name="bearerAuth") public FlightResponse edit(@PathVariable Long id,@Valid @RequestBody EditFlight r){return service.edit(id,r);}
}
