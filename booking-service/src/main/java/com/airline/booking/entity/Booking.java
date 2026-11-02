package com.airline.booking.entity;

import com.airline.booking.domain.BookingStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity @Table(name="bookings")
public class Booking {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
  @Column(name="user_id",nullable=false) public Long userId;
  @Column(name="flight_id",nullable=false) public Long flightId;
  @Column(name="seat_number",nullable=false) public int seatNumber;
  @Column(name="fare_at_booking",nullable=false,precision=10,scale=2) public BigDecimal fareAtBooking;
  @Enumerated(EnumType.STRING)
  @Column(nullable=false,length=20)
  public BookingStatus status = BookingStatus.CONFIRMED;
  @Column(name="booked_at",nullable=false) public LocalDateTime bookedAt=LocalDateTime.now(java.time.Clock.systemUTC());
  protected Booking() {}
  public Booking(Long userId,Long flightId,int seat,BigDecimal price){this.userId=userId;this.flightId=flightId;this.seatNumber=seat;this.fareAtBooking=price;}
}
