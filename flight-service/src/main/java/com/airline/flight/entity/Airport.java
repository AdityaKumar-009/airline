package com.airline.flight.entity;
import jakarta.persistence.*;
@Entity @Table(name="airports")
public class Airport {
  @Id @Column(length=3) public String code;
  @Column(nullable=false,length=120) public String name;
  @Column(nullable=false,length=100) public String city;
  protected Airport() {}
}

