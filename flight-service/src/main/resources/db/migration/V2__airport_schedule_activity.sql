CREATE TABLE airports (code VARCHAR(3) NOT NULL PRIMARY KEY, name VARCHAR(120) NOT NULL, city VARCHAR(100) NOT NULL);
INSERT INTO airports (code, name, city) VALUES
('DEL','Indira Gandhi International Airport','Delhi'),
('BOM','Chhatrapati Shivaji Maharaj International Airport','Mumbai'),
('BLR','Kempegowda International Airport','Bengaluru'),
('HYD','Rajiv Gandhi International Airport','Hyderabad'),
('MAA','Chennai International Airport','Chennai'),
('GOI','Goa International Airport','Goa'),
('CCU','Netaji Subhas Chandra Bose International Airport','Kolkata'),
('JAI','Jaipur International Airport','Jaipur'),
('PNQ','Pune Airport','Pune');
ALTER TABLE flights
  ADD COLUMN origin_code VARCHAR(3) NULL,
  ADD COLUMN destination_code VARCHAR(3) NULL,
  ADD COLUMN aircraft_type VARCHAR(40) NOT NULL DEFAULT 'A320',
  ADD COLUMN operational_status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED';
UPDATE flights SET
  origin_code = CASE LOWER(origin) WHEN 'delhi' THEN 'DEL' WHEN 'mumbai' THEN 'BOM'
    WHEN 'bengaluru' THEN 'BLR' WHEN 'hyderabad' THEN 'HYD' WHEN 'chennai' THEN 'MAA'
    WHEN 'goa' THEN 'GOI' WHEN 'kolkata' THEN 'CCU' WHEN 'jaipur' THEN 'JAI'
    WHEN 'pune' THEN 'PNQ' ELSE NULL END,
  destination_code = CASE LOWER(destination) WHEN 'delhi' THEN 'DEL' WHEN 'mumbai' THEN 'BOM'
    WHEN 'bengaluru' THEN 'BLR' WHEN 'hyderabad' THEN 'HYD' WHEN 'chennai' THEN 'MAA'
    WHEN 'goa' THEN 'GOI' WHEN 'kolkata' THEN 'CCU' WHEN 'jaipur' THEN 'JAI'
    WHEN 'pune' THEN 'PNQ' ELSE NULL END;
CREATE TABLE flight_activities (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  flight_id BIGINT NOT NULL,
  action VARCHAR(40) NOT NULL,
  description VARCHAR(255) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  CONSTRAINT fk_activity_flight FOREIGN KEY (flight_id) REFERENCES flights(id)
);

