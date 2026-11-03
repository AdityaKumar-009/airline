package com.airline.booking.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "booking_passengers")
public class BookingPassenger {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "booking_id", nullable = false)
  public Booking booking;
  @Column(name = "full_name", nullable = false, length = 100) public String fullName;
  @Column(nullable = false) public int age;
  @Column(name = "seat_number", nullable = false) public int seatNumber;
  @Column(nullable = false, precision = 10, scale = 2) public BigDecimal fare;

  protected BookingPassenger() {}

  public BookingPassenger(String fullName, int age, int seatNumber, BigDecimal fare) {
    this.fullName = fullName;
    this.age = age;
    this.seatNumber = seatNumber;
    this.fare = fare;
  }
}

