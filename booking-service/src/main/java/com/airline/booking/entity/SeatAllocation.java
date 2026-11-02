package com.airline.booking.entity;
import jakarta.persistence.*;
@Entity @Table(name="seat_allocations",uniqueConstraints=@UniqueConstraint(columnNames={"flight_id","seat_number"}))
public class SeatAllocation {
  @Id @Column(name="booking_id") public Long bookingId;
  @Column(name="flight_id",nullable=false) public Long flightId;
  @Column(name="seat_number",nullable=false) public int seatNumber;
  protected SeatAllocation() {}
  public SeatAllocation(Long bookingId,Long flightId,int seat){this.bookingId=bookingId;this.flightId=flightId;this.seatNumber=seat;}
}
