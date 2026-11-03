package com.airline.booking.controller;
import com.airline.booking.dto.BookingDtos.*;
import com.airline.booking.domain.BookingStatus;
import com.airline.booking.service.BookingService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
@RestController @RequestMapping("/api/bookings")
public class BookingController {
  private final BookingService service;
  public BookingController(BookingService service){this.service=service;}
  @GetMapping("/seats") public List<Integer> seats(@RequestParam Long flightId){return service.occupied(flightId);}
  @GetMapping("/me") @SecurityRequirement(name="bearerAuth") public List<BookingResponse> mine(JwtAuthenticationToken jwt){return service.mine(Long.valueOf(jwt.getToken().getSubject()));}
  @GetMapping("/admin") @SecurityRequirement(name="bearerAuth")
  public List<AdminBookingResponse> adminBookings(
      @RequestParam(required=false) String flightNumber,
      @RequestParam(required=false) BookingStatus status){return service.adminBookings(flightNumber,status);}
  @PostMapping @ResponseStatus(HttpStatus.CREATED) @SecurityRequirement(name="bearerAuth") public BookingResponse book(JwtAuthenticationToken jwt,@Valid @RequestBody BookingRequest r){
    return service.book(Long.valueOf(jwt.getToken().getSubject()),r);
  }
  @PatchMapping("/{id}/cancel") @SecurityRequirement(name="bearerAuth") public BookingResponse cancel(JwtAuthenticationToken jwt,@PathVariable Long id){
    return service.cancel(Long.valueOf(jwt.getToken().getSubject()),id);
  }
}
