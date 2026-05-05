package com.harsh.rentify.service;

import com.harsh.rentify.entity.ReservationEntity;
import com.harsh.rentify.entity.ReservationStatus;
import com.harsh.rentify.entity.UserEntity;
import com.harsh.rentify.exception.BusinessException;
import com.harsh.rentify.exception.NotFoundException;
import com.harsh.rentify.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {

    private final ReservationRepository reservationRepository;

    public BookingService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public void cancelBooking(Long bookingId, UserEntity tenant) {
        ReservationEntity booking = reservationRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking with id " + bookingId + " was not found."));
        if (!booking.getTenant().getId().equals(tenant.getId())) {
            throw new BusinessException("You can cancel only your own bookings.");
        }
        if (booking.isPaid()) {
            throw new BusinessException("Paid bookings cannot be cancelled.");
        }
        if (booking.getStatus() != ReservationStatus.PENDING && booking.getStatus() != ReservationStatus.APPROVED) {
            throw new BusinessException("Only pending or approved bookings can be cancelled.");
        }

        booking.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(booking);
    }
}
