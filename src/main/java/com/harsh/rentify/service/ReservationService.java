package com.harsh.rentify.service;

import com.harsh.rentify.dto.request.BookingRequest;
import com.harsh.rentify.dto.response.ReservationViewResponse;
import com.harsh.rentify.entity.ReservationEntity;
import com.harsh.rentify.entity.ReservationStatus;
import com.harsh.rentify.entity.Role;
import com.harsh.rentify.entity.RoomEntity;
import com.harsh.rentify.entity.UserEntity;
import com.harsh.rentify.exception.BusinessException;
import com.harsh.rentify.exception.NotFoundException;
import com.harsh.rentify.mapper.RoomMapper;
import com.harsh.rentify.repository.ReservationRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomService roomService;
    private final RoomMapper roomMapper;

    public ReservationService(
            ReservationRepository reservationRepository,
            RoomService roomService,
            RoomMapper roomMapper
    ) {
        this.reservationRepository = reservationRepository;
        this.roomService = roomService;
        this.roomMapper = roomMapper;
    }

    @Transactional
    public ReservationViewResponse createReservation(Long roomId, BookingRequest request, UserEntity tenant) {
        if (tenant.getRole() != Role.TENANT) {
            throw new BusinessException("Only tenant accounts can place bookings.");
        }

        RoomEntity room = roomService.getRoomEntityOrThrow(roomId);
        validateReservationRequest(room, request, tenant);

        long nights = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
        BigDecimal nightly = room.getOvernightPrice().multiply(BigDecimal.valueOf(nights));
        BigDecimal extraGuests = room.getExtraPersonPrice()
                .multiply(BigDecimal.valueOf(Math.max(request.getGuests() - 1, 0L)))
                .multiply(BigDecimal.valueOf(nights));

        ReservationEntity reservation = new ReservationEntity();
        reservation.setRoom(room);
        reservation.setTenant(tenant);
        reservation.setCheckInDate(request.getCheckInDate());
        reservation.setCheckOutDate(request.getCheckOutDate());
        reservation.setGuests(request.getGuests());
        reservation.setTotalCost(nightly.add(extraGuests));
        reservation.setStatus(ReservationStatus.PENDING);

        return roomMapper.toReservation(reservationRepository.save(reservation));
    }

    @Transactional(readOnly = true)
    public List<ReservationViewResponse> getTenantReservations(UserEntity tenant) {
        return reservationRepository.findByTenantOrderByCreatedAtDesc(tenant).stream()
                .map(roomMapper::toReservation)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationViewResponse> getLandlordReservations(UserEntity landlord) {
        return reservationRepository.findByRoomHostOrderByCreatedAtDesc(landlord).stream()
                .map(roomMapper::toReservation)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationViewResponse> getAllReservations() {
        return reservationRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(roomMapper::toReservation)
                .toList();
    }

    @Transactional
    public void approveReservation(Long reservationId, UserEntity landlord) {
        ReservationEntity reservation = getReservationForLandlord(reservationId, landlord);
        reservation.setStatus(ReservationStatus.APPROVED);
        reservation.setRejectReason(null);
    }

    @Transactional
    public void rejectReservation(Long reservationId, UserEntity landlord) {
        ReservationEntity reservation = getReservationForLandlord(reservationId, landlord);
        reservation.setStatus(ReservationStatus.REJECTED);
        reservation.setRejectReason(null);
        reservationRepository.save(reservation);
    }

    @Transactional(readOnly = true)
    public long countReservations() {
        return reservationRepository.count();
    }

    @Transactional(readOnly = true)
    public boolean hasApprovedReservation(UserEntity tenant, RoomEntity room) {
        return reservationRepository.findByTenantOrderByCreatedAtDesc(tenant).stream()
                .anyMatch(reservation -> reservation.getRoom().getId().equals(room.getId())
                        && reservation.getStatus() == ReservationStatus.APPROVED);
    }

    private void validateReservationRequest(RoomEntity room, BookingRequest request, UserEntity tenant) {
        LocalDate today = LocalDate.now();
        if (request.getCheckInDate().isBefore(today)) {
            throw new BusinessException("Check-in cannot be in the past.");
        }
        if (!request.getCheckOutDate().isAfter(request.getCheckInDate())) {
            throw new BusinessException("Check-out must be after check-in.");
        }
        if (request.getGuests() > room.getMaxPeople()) {
            throw new BusinessException("Guest count exceeds the listing capacity.");
        }
        long nights = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
        if (nights < room.getMinOvernights()) {
            throw new BusinessException("This listing requires at least " + room.getMinOvernights() + " nights.");
        }
        if (room.getHost().getId().equals(tenant.getId())) {
            throw new BusinessException("You cannot book your own listing.");
        }
        if (!roomService.isAvailable(room, request.getCheckInDate(), request.getCheckOutDate())) {
            throw new BusinessException("Those dates are not available.");
        }
    }

    private ReservationEntity getReservationForLandlord(Long reservationId, UserEntity landlord) {
        ReservationEntity reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("Reservation with id " + reservationId + " was not found."));
        if (!reservation.getRoom().getHost().getId().equals(landlord.getId())) {
            throw new BusinessException("You can manage only reservations for your own listings.");
        }
        return reservation;
    }
}
