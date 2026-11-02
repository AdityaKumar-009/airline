CREATE TABLE bookings (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  flight_id BIGINT NOT NULL,
  seat_number INT NOT NULL,
  fare_at_booking DECIMAL(10,2) NOT NULL,
  status VARCHAR(20) NOT NULL,
  booked_at DATETIME(6) NOT NULL
);
CREATE TABLE seat_allocations (
  booking_id BIGINT NOT NULL PRIMARY KEY,
  flight_id BIGINT NOT NULL,
  seat_number INT NOT NULL,
  CONSTRAINT fk_allocation_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
  CONSTRAINT uq_flight_seat UNIQUE (flight_id,seat_number)
);
