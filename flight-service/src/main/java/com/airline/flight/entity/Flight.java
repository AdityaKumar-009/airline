package com.airline.flight.entity;
import com.airline.flight.domain.FlightStatus;
import com.airline.flight.domain.OperationalStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity @Table(name="flights",uniqueConstraints=@UniqueConstraint(columnNames={"flight_number","departure_time"}))
public class Flight {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
  @Column(name="flight_number",nullable=false,length=20) public String flightNumber;
  @Column(nullable=false,length=100) public String origin;
  @Column(nullable=false,length=100) public String destination;
  @Column(name="origin_code",length=3) public String originCode;
  @Column(name="destination_code",length=3) public String destinationCode;
  @Column(name="aircraft_type",nullable=false,length=40) public String aircraftType="A320";
  @Column(name="departure_time",nullable=false) public LocalDateTime departureTime;
  @Column(name="arrival_time",nullable=false) public LocalDateTime arrivalTime;
  @Column(nullable=false,precision=10,scale=2) public BigDecimal price;
  @Column(nullable=false) public int capacity;
  @Enumerated(EnumType.STRING) @Column(nullable=false,length=20)
  public FlightStatus status=FlightStatus.OPEN;
  @Enumerated(EnumType.STRING) @Column(name="operational_status",nullable=false,length=20)
  public OperationalStatus operationalStatus=OperationalStatus.SCHEDULED;
  protected Flight() {}
  public Flight(String number,String origin,String destination,LocalDateTime departure,LocalDateTime arrival,BigDecimal price,int capacity){
    this.flightNumber=number;this.origin=origin;this.destination=destination;
    this.originCode=codeFor(origin);this.destinationCode=codeFor(destination);
    this.departureTime=departure;this.arrivalTime=arrival;this.price=price;this.capacity=capacity;
  }
  private static String codeFor(String city){
    return switch(city.toLowerCase(java.util.Locale.ROOT)){
      case "delhi" -> "DEL";case "mumbai" -> "BOM";case "bengaluru" -> "BLR";
      case "hyderabad" -> "HYD";case "chennai" -> "MAA";case "goa" -> "GOI";
      case "kolkata" -> "CCU";case "jaipur" -> "JAI";case "pune" -> "PNQ";
      default -> null;
    };
  }
}

