package com.airline.booking.entity;

import com.airline.booking.domain.BookingStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
public class Booking {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
  @Column(name = "user_id", nullable = false) public Long userId;
  @Column(name = "flight_id", nullable = false) public Long flightId;
  @Column(name = "seat_number", nullable = false) public int seatNumber;
  // Historic column name retained to allow upgrade of the original demo database.
  @Column(name = "fare_at_booking", nullable = false, precision = 10, scale = 2)
  public BigDecimal totalFare;
  @Column(nullable = false, length = 12, unique = true) public String pnr;
  @Column(name = "contact_email", length = 150) public String contactEmail;
  @Column(name = "contact_phone", length = 20) public String contactPhone;
  @Column(name = "flight_number", length = 20) public String flightNumber;
  @Column(length = 100) public String origin;
  @Column(length = 100) public String destination;
  @Column(name = "origin_code", length = 3) public String originCode;
  @Column(name = "destination_code", length = 3) public String destinationCode;
  @Column(name = "departure_time") public LocalDateTime departureTime;
  @Column(name = "arrival_time") public LocalDateTime arrivalTime;
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  public BookingStatus status = BookingStatus.CONFIRMED;
  @Column(name = "booked_at", nullable = false)
  public LocalDateTime bookedAt = LocalDateTime.now(Clock.systemUTC());

  @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("id ASC")
  public List<BookingPassenger> passengers = new ArrayList<>();

  protected Booking() {}

  public Booking(Long userId, Long flightId, int firstSeat, BigDecimal totalFare, String pnr,
                 String email, String phone, String flightNumber, String origin, String destination,
                 String originCode, String destinationCode, LocalDateTime departure, LocalDateTime arrival) {
    this.userId = userId;
    this.flightId = flightId;
    this.seatNumber = firstSeat;
    this.totalFare = totalFare;
    this.pnr = pnr;
    this.contactEmail = email;
    this.contactPhone = phone;
    this.flightNumber = flightNumber;
    this.origin = origin;
    this.destination = destination;
    this.originCode = originCode;
    this.destinationCode = destinationCode;
    this.departureTime = departure;
    this.arrivalTime = arrival;
  }

  public void addPassenger(BookingPassenger passenger) {
    passenger.booking = this;
    passengers.add(passenger);
  }
}

