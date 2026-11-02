CREATE TABLE flights (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  flight_number VARCHAR(20) NOT NULL,
  origin VARCHAR(100) NOT NULL,
  destination VARCHAR(100) NOT NULL,
  departure_time DATETIME(6) NOT NULL,
  arrival_time DATETIME(6) NOT NULL,
  price DECIMAL(10,2) NOT NULL,
  capacity INT NOT NULL,
  status VARCHAR(20) NOT NULL,
  CONSTRAINT uq_flight_time UNIQUE (flight_number, departure_time)
);
