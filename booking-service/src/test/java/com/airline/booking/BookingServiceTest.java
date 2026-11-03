package com.airline.booking;

import com.airline.booking.domain.BookingStatus;
import com.airline.booking.entity.Booking;
import com.airline.booking.entity.SeatAllocation;
import com.airline.booking.repository.*;
import com.airline.booking.service.BookingServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingServiceTest {
  private final BookingRepository bookings = mock(BookingRepository.class);
  private final SeatAllocationRepository allocations = mock(SeatAllocationRepository.class);
  private final BookingServiceImpl service = new BookingServiceImpl(
      bookings, allocations, RestClient.builder());

  @Test
  void occupiedSeatsAreSorted() {
    when(allocations.findByFlightId(5L)).thenReturn(List.of(
        new SeatAllocation(9L, 5L, 12),
        new SeatAllocation(7L, 5L, 2)));
    assertEquals(List.of(2, 12), service.occupied(5L));
  }

  @Test
  void adminReportFiltersByFlightAndStatus() {
    Booking confirmed = sample(1L, 5L, 3, "ABCDEFGH");
    Booking cancelled = sample(2L, 5L, 4, "BCDEFGHJ");
    cancelled.status = BookingStatus.CANCELLED;
    Booking otherFlight = sample(3L, 6L, 1, "CDEFGHJK", "AI202");
    when(bookings.findAllByOrderByBookedAtDesc())
        .thenReturn(List.of(confirmed, cancelled, otherFlight));

    var filtered = service.adminBookings(" ai101 ", BookingStatus.CANCELLED);
    assertEquals(1, filtered.size());
    assertEquals(cancelled.pnr, filtered.get(0).pnr());
    assertEquals(BookingStatus.CANCELLED, filtered.get(0).status());
    assertEquals(5L, filtered.get(0).flightId());
  }

  private Booking sample(long userId, long flightId, int seat, String pnr) {
    return sample(userId, flightId, seat, pnr, "AI101");
  }

  private Booking sample(long userId, long flightId, int seat, String pnr, String flightNumber) {
    LocalDateTime departure = LocalDateTime.now().plusDays(5);
    return new Booking(userId, flightId, seat, new BigDecimal("4500"), pnr,
        "customer@example.test", null, flightNumber, "Delhi", "Mumbai",
        "DEL", "BOM", departure, departure.plusHours(2));
  }
}
