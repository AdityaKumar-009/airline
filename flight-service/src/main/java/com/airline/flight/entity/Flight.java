package com.airline.flight.entity;

import com.airline.flight.domain.FlightStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity @Table(name="flights",uniqueConstraints=@UniqueConstraint(columnNames={"flight_number","departure_time"}))
public class Flight {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
  @Column(name="flight_number",nullable=false,length=20) public String flightNumber;
  @Column(nullable=false,length=100) public String origin;
  @Column(nullable=false,length=100) public String destination;
  @Column(name="departure_time",nullable=false) public LocalDateTime departureTime;
  @Column(name="arrival_time",nullable=false) public LocalDateTime arrivalTime;
  @Column(nullable=false,precision=10,scale=2) public BigDecimal price;
  @Column(nullable=false) public int capacity;
  @Enumerated(EnumType.STRING)
  @Column(nullable=false,length=20)
  public FlightStatus status = FlightStatus.OPEN;
  protected Flight() {}
  public Flight(String number,String origin,String destination,LocalDateTime departure,LocalDateTime arrival,BigDecimal price,int capacity){
    this.flightNumber=number;this.origin=origin;this.destination=destination;this.departureTime=departure;this.arrivalTime=arrival;this.price=price;this.capacity=capacity;
  }
}
