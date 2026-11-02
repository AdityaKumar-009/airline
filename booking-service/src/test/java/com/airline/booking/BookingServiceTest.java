package com.airline.booking;
import com.airline.booking.entity.SeatAllocation;
import com.airline.booking.entity.Booking;
import com.airline.booking.domain.BookingStatus;
import com.airline.booking.repository.*;
import com.airline.booking.service.BookingServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import java.util.List;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class BookingServiceTest {
  @Test void occupiedSeatsAreSorted(){
    var bookings=mock(BookingRepository.class);
    var allocations=mock(SeatAllocationRepository.class);
    when(allocations.findByFlightId(5L)).thenReturn(List.of(new SeatAllocation(9L,5L,12),new SeatAllocation(7L,5L,2)));
    var service=new BookingServiceImpl(bookings,allocations,RestClient.builder());
    assertEquals(List.of(2,12),service.occupied(5L));
  }

  @Test void adminBookingReportFiltersByFlightAndStatus(){
    var bookings=mock(BookingRepository.class);
    var allocations=mock(SeatAllocationRepository.class);
    var confirmed=new Booking(7L,5L,3,new BigDecimal("120.00"));
    confirmed.id=11L;
    var cancelled=new Booking(8L,5L,4,new BigDecimal("140.00"));
    cancelled.id=12L;
    cancelled.status=BookingStatus.CANCELLED;
    var otherFlight=new Booking(9L,6L,1,new BigDecimal("160.00"));
    otherFlight.id=13L;
    when(bookings.findAllByOrderByBookedAtDesc()).thenReturn(List.of(confirmed,cancelled,otherFlight));
    var service=new BookingServiceImpl(bookings,allocations,RestClient.builder());

    var result=service.adminBookings(5L,BookingStatus.CANCELLED);

    assertEquals(1,result.size());
    assertEquals(12L,result.get(0).id());
    assertEquals(8L,result.get(0).userId());
    assertEquals(BookingStatus.CANCELLED,result.get(0).status());
  }
}
