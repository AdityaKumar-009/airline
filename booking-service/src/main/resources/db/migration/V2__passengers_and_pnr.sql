ALTER TABLE bookings
  ADD COLUMN pnr VARCHAR(12) NULL,
  ADD COLUMN contact_email VARCHAR(150) NULL,
  ADD COLUMN contact_phone VARCHAR(20) NULL,
  ADD COLUMN flight_number VARCHAR(20) NULL,
  ADD COLUMN origin VARCHAR(100) NULL,
  ADD COLUMN destination VARCHAR(100) NULL,
  ADD COLUMN origin_code VARCHAR(3) NULL,
  ADD COLUMN destination_code VARCHAR(3) NULL,
  ADD COLUMN departure_time DATETIME(6) NULL,
  ADD COLUMN arrival_time DATETIME(6) NULL;

-- Old single-seat records remain accessible after the upgrade.
UPDATE bookings SET pnr = CONCAT('L', LPAD(id, 8, '0')) WHERE pnr IS NULL;
ALTER TABLE bookings
  MODIFY pnr VARCHAR(12) NOT NULL,
  ADD CONSTRAINT uq_booking_pnr UNIQUE (pnr);

-- Keep a separate index for the existing FK before replacing the old primary key.
ALTER TABLE seat_allocations ADD INDEX idx_allocation_booking (booking_id);
ALTER TABLE seat_allocations DROP PRIMARY KEY;
ALTER TABLE seat_allocations ADD COLUMN id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST;

CREATE TABLE booking_passengers (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  booking_id BIGINT NOT NULL,
  full_name VARCHAR(100) NOT NULL,
  age INT NOT NULL,
  seat_number INT NOT NULL,
  fare DECIMAL(10,2) NOT NULL,
  CONSTRAINT fk_passenger_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
  CONSTRAINT uq_booking_passenger_seat UNIQUE (booking_id, seat_number)
);

INSERT INTO booking_passengers (booking_id, full_name, age, seat_number, fare)
SELECT id, 'Legacy Passenger', 18, seat_number, fare_at_booking FROM bookings;

