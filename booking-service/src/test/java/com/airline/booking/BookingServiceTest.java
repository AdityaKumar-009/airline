package com.airline.booking;

import com.airline.booking.entity.SeatAllocation;
import com.airline.booking.repository.BookingRepository;
import com.airline.booking.repository.SeatAllocationRepository;
import com.airline.booking.service.BookingServiceImpl;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class BookingServiceTest {
  @Test
  void occupiedSeatsAreSorted() {
    var bookings = mock(BookingRepository.class);
    var seats = mock(SeatAllocationRepository.class);
    when(seats.findByFlightId(5L)).thenReturn(List.of(
        new SeatAllocation(9L, 5L, 12), new SeatAllocation(7L, 5L, 2)));
    var service = new BookingServiceImpl(bookings, seats, RestClient.builder());
    assertEquals(List.of(2, 12), service.occupied(5L));
  }
}

