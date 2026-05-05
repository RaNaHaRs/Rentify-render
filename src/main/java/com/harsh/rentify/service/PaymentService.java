package com.harsh.rentify.service;

import com.harsh.rentify.dto.response.PaymentViewResponse;
import com.harsh.rentify.entity.PaymentEntity;
import com.harsh.rentify.entity.PaymentStatus;
import com.harsh.rentify.entity.ReservationEntity;
import com.harsh.rentify.entity.ReservationStatus;
import com.harsh.rentify.entity.UserEntity;
import com.harsh.rentify.exception.BusinessException;
import com.harsh.rentify.exception.NotFoundException;
import com.harsh.rentify.mapper.UserMapper;
import com.harsh.rentify.repository.PaymentRepository;
import com.harsh.rentify.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final UserMapper userMapper;

    public PaymentService(
            PaymentRepository paymentRepository,
            ReservationRepository reservationRepository,
            UserMapper userMapper
    ) {
        this.paymentRepository = paymentRepository;
        this.reservationRepository = reservationRepository;
        this.userMapper = userMapper;
    }

    @Transactional(readOnly = true)
    public PaymentViewResponse getPaymentView(Long bookingId, UserEntity tenant) {
        ReservationEntity booking = getBookingForTenant(bookingId, tenant);
        if (booking.isPaid()) {
            throw new BusinessException("Payment for this booking has already been completed.");
        }

        PaymentViewResponse response = new PaymentViewResponse();
        response.setBookingId(booking.getId());
        response.setPropertyName(booking.getRoom().getTitle());
        response.setLandlordName(userMapper.resolveDisplayName(booking.getRoom().getHost()));
        response.setTenantName(userMapper.resolveDisplayName(booking.getTenant()));
        response.setAmount(booking.getTotalCost());
        return response;
    }

    @Transactional
    public void processPayment(Long bookingId, UserEntity tenant) {
        ReservationEntity booking = getBookingForTenant(bookingId, tenant);
        if (booking.isPaid() || paymentRepository.existsByBookingId(bookingId)) {
            throw new BusinessException("Payment for this booking has already been completed.");
        }

        PaymentEntity payment = new PaymentEntity();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalCost());
        payment.setStatus(PaymentStatus.PAID);
        paymentRepository.save(payment);

        booking.setPaid(true);
        reservationRepository.save(booking);
    }

    private ReservationEntity getBookingForTenant(Long bookingId, UserEntity tenant) {
        ReservationEntity booking = reservationRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking with id " + bookingId + " was not found."));
        if (!booking.getTenant().getId().equals(tenant.getId())) {
            throw new BusinessException("You can only pay for your own bookings.");
        }
        if (booking.getStatus() != ReservationStatus.APPROVED) {
            throw new BusinessException("Payment is only available for approved bookings.");
        }
        return booking;
    }
}
