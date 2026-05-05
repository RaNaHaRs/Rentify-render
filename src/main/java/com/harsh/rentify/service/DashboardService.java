package com.harsh.rentify.service;

import com.harsh.rentify.dto.response.AdminDashboardResponse;
import com.harsh.rentify.dto.response.LandlordDashboardResponse;
import com.harsh.rentify.dto.response.ReservationViewResponse;
import com.harsh.rentify.dto.response.TenantDashboardResponse;
import com.harsh.rentify.entity.PaymentEntity;
import com.harsh.rentify.entity.Role;
import com.harsh.rentify.entity.ReservationStatus;
import com.harsh.rentify.entity.UserEntity;
import com.harsh.rentify.repository.PaymentRepository;
import com.harsh.rentify.repository.RoomRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {
    private static final DateTimeFormatter REVENUE_MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);

    private final RoomService roomService;
    private final ReservationService reservationService;
    private final UserService userService;
    private final RoomRepository roomRepository;
    private final PaymentRepository paymentRepository;

    public DashboardService(
            RoomService roomService,
            ReservationService reservationService,
            UserService userService,
            RoomRepository roomRepository,
            PaymentRepository paymentRepository
    ) {
        this.roomService = roomService;
        this.reservationService = reservationService;
        this.userService = userService;
        this.roomRepository = roomRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional(readOnly = true)
    public LandlordDashboardResponse getLandlordDashboard(UserEntity landlord) {
        List<ReservationViewResponse> reservations = reservationService.getLandlordReservations(landlord);
        LandlordDashboardResponse response = new LandlordDashboardResponse();
        response.setListings(roomService.getListingsForLandlord(landlord));
        response.setReservations(reservations);
        response.setListingsCount(response.getListings().size());
        response.setTotalBookings(reservations.size());
        response.setPendingReservations((int) reservations.stream()
                .filter(reservation -> reservation.getStatus() == ReservationStatus.PENDING)
                .count());
        response.setApprovedReservations((int) reservations.stream()
                .filter(reservation -> reservation.getStatus() == ReservationStatus.APPROVED)
                .count());
        response.setTotalRevenue(nullToZero(paymentRepository.sumAmountByLandlordId(landlord.getId())));
        return response;
    }

    @Transactional(readOnly = true)
    public TenantDashboardResponse getTenantDashboard(UserEntity tenant) {
        List<ReservationViewResponse> reservations = reservationService.getTenantReservations(tenant);
        TenantDashboardResponse response = new TenantDashboardResponse();
        response.setReservations(reservations);
        response.setAvailableListings(roomService.getAllListings());
        response.setReservationsCount(reservations.size());
        response.setUpcomingReservations((int) reservations.stream()
                .filter(reservation -> reservation.getCheckInDate().isAfter(LocalDate.now()))
                .count());
        response.setApprovedReservations((int) reservations.stream()
                .filter(reservation -> reservation.getStatus() == ReservationStatus.APPROVED)
                .count());
        response.setCancelledReservations((int) reservations.stream()
                .filter(reservation -> reservation.getStatus() == ReservationStatus.CANCELLED)
                .count());
        response.setTotalSpend(nullToZero(paymentRepository.sumAmountByTenantId(tenant.getId())));
        return response;
    }

    @Transactional(readOnly = true)
    public AdminDashboardResponse getAdminDashboard() {
        AdminDashboardResponse response = new AdminDashboardResponse();
        response.setTotalUsers(userService.countUsers());
        response.setTotalLandlords(userService.countUsersByRole(Role.LANDLORD));
        response.setTotalTenants(userService.countUsersByRole(Role.TENANT));
        response.setTotalAdmins(userService.countUsersByRole(Role.ADMIN));
        response.setTotalRooms(roomRepository.count());
        response.setTotalReservations(reservationService.countReservations());
        response.setPendingLandlords(userService.countPendingLandlords());
        response.setTotalRevenue(nullToZero(paymentRepository.sumAllAmounts()));
        populateRevenueTrend(response);
        response.setLandlordsAwaitingApproval(userService.getPendingLandlords());
        response.setLandlords(userService.getUsersByRole(Role.LANDLORD));
        response.setTenants(userService.getUsersByRole(Role.TENANT));
        return response;
    }

    private void populateRevenueTrend(AdminDashboardResponse response) {
        Map<YearMonth, BigDecimal> revenueByMonth = paymentRepository.findAllByOrderByPaymentDateAsc().stream()
                .collect(Collectors.groupingBy(
                        payment -> YearMonth.from(payment.getPaymentDate()),
                        TreeMap::new,
                        Collectors.mapping(
                                PaymentEntity::getAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));

        response.setRevenueTrendLabels(revenueByMonth.keySet().stream()
                .map(month -> month.format(REVENUE_MONTH_FORMATTER))
                .toList());
        response.setRevenueTrendValues(new ArrayList<>(revenueByMonth.values()));
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
