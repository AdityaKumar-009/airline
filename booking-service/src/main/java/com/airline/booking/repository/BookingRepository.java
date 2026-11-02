package com.airline.booking.repository;
import com.airline.booking.entity.Booking;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface BookingRepository extends JpaRepository<Booking,Long>{
  List<Booking> findByUserIdOrderByBookedAtDesc(Long userId);
  List<Booking> findAllByOrderByBookedAtDesc();
}
