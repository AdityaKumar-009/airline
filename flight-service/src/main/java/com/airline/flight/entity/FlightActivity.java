package com.airline.flight.entity;
import jakarta.persistence.*;
import java.time.*;
@Entity @Table(name="flight_activities")
public class FlightActivity {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
  @Column(name="flight_id",nullable=false) public Long flightId;
  @Column(nullable=false,length=40) public String action;
  @Column(nullable=false,length=255) public String description;
  @Column(name="created_at",nullable=false) public LocalDateTime createdAt=LocalDateTime.now(Clock.systemUTC());
  protected FlightActivity() {}
  public FlightActivity(Long flightId,String action,String description) {
    this.flightId=flightId;this.action=action;this.description=description;
  }
}

