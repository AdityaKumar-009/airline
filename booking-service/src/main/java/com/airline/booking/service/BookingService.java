package com.airline.booking.service;

import com.airline.booking.dto.BookingDtos.BookingRequest;
import com.airline.booking.dto.BookingDtos.BookingResponse;
import com.airline.booking.dto.BookingDtos.AdminBookingResponse;
import com.airline.booking.domain.BookingStatus;
import java.util.List;

public interface BookingService {
  List<Integer> occupied(Long flightId);

  List<BookingResponse> mine(Long userId);

  List<AdminBookingResponse> adminBookings(Long flightId, BookingStatus status);

  BookingResponse book(Long userId, BookingRequest request);

  BookingResponse cancel(Long userId, Long bookingId);
}
