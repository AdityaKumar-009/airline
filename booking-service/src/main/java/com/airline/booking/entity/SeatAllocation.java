package com.airline.booking.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "seat_allocations", uniqueConstraints = @UniqueConstraint(columnNames = {"flight_id", "seat_number"}))
public class SeatAllocation {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
  @Column(name = "booking_id", nullable = false) public Long bookingId;
  @Column(name = "flight_id", nullable = false) public Long flightId;
  @Column(name = "seat_number", nullable = false) public int seatNumber;

  protected SeatAllocation() {}
  public SeatAllocation(Long bookingId, Long flightId, int seatNumber) {
    this.bookingId = bookingId;
    this.flightId = flightId;
    this.seatNumber = seatNumber;
  }
}

