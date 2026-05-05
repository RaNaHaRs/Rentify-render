package com.harsh.rentify.controller;

import com.harsh.rentify.dto.request.RoomSearchRequest;
import com.harsh.rentify.dto.response.LandlordDashboardResponse;
import com.harsh.rentify.dto.response.TenantDashboardResponse;
import com.harsh.rentify.dto.response.UserSummaryResponse;
import com.harsh.rentify.entity.ReservationStatus;
import com.harsh.rentify.entity.UserEntity;
import com.harsh.rentify.service.DashboardService;
import com.harsh.rentify.service.ReservationService;
import com.harsh.rentify.service.RoomService;
import com.harsh.rentify.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserService userService;
    private final ReservationService reservationService;
    private final RoomService roomService;

    public DashboardController(
            DashboardService dashboardService,
            UserService userService,
            ReservationService reservationService,
            RoomService roomService
    ) {
        this.dashboardService = dashboardService;
        this.userService = userService;
        this.reservationService = reservationService;
        this.roomService = roomService;
    }

    @GetMapping("/landlord/dashboard")
    @PreAuthorize("hasRole('LANDLORD')")
    public String landlordDashboard(Model model) {
        UserEntity landlord = userService.getCurrentUserEntityOrThrow();
        LandlordDashboardResponse dashboard = dashboardService.getLandlordDashboard(landlord);
        populateLandlordDashboardModel(model, userService.getCurrentUserSummary(), dashboard);
        return "dashboard/landlord";
    }

    @GetMapping("/tenant/dashboard")
    @PreAuthorize("hasRole('TENANT')")
    public String tenantDashboard(Model model) {
        UserEntity tenant = userService.getCurrentUserEntityOrThrow();
        TenantDashboardResponse dashboard = dashboardService.getTenantDashboard(tenant);
        populateTenantDashboardModel(model, userService.getCurrentUserSummary(), dashboard);
        return "dashboard/tenant";
    }

    @GetMapping("/tenant/bookings")
    @PreAuthorize("hasRole('TENANT')")
    public String tenantBookings(Model model) {
        UserEntity tenant = userService.getCurrentUserEntityOrThrow();
        TenantDashboardResponse dashboard = dashboardService.getTenantDashboard(tenant);
        model.addAttribute("loggedInUser", userService.getCurrentUserSummary());
        model.addAttribute("reservations", dashboard.getReservations());
        model.addAttribute("totalBookings", dashboard.getReservationsCount());
        return "tenant/bookings";
    }

    @GetMapping({"/tenant/properties", "/properties"})
    @PreAuthorize("hasRole('TENANT')")
    public String tenantProperties(@ModelAttribute("search") RoomSearchRequest search, Model model) {
        model.addAttribute("loggedInUser", userService.getCurrentUserSummary());
        model.addAttribute("properties", roomService.searchRooms(search));
        model.addAttribute("priceOptions", roomService.getPriceFilterOptions());
        return "tenant/properties";
    }

    @GetMapping("/tenant/payments")
    @PreAuthorize("hasRole('TENANT')")
    public String tenantPayments(Model model) {
        UserEntity tenant = userService.getCurrentUserEntityOrThrow();
        TenantDashboardResponse dashboard = dashboardService.getTenantDashboard(tenant);
        model.addAttribute("loggedInUser", userService.getCurrentUserSummary());
        model.addAttribute("reservations", dashboard.getReservations().stream()
                .filter(reservation -> reservation.getStatus() == ReservationStatus.APPROVED || reservation.isPaid())
                .toList());
        return "tenant/payments";
    }

    @GetMapping("/landlord/properties")
    @PreAuthorize("hasRole('LANDLORD')")
    public String landlordProperties(Model model) {
        UserEntity landlord = userService.getCurrentUserEntityOrThrow();
        LandlordDashboardResponse dashboard = dashboardService.getLandlordDashboard(landlord);
        model.addAttribute("loggedInUser", userService.getCurrentUserSummary());
        model.addAttribute("properties", dashboard.getListings());
        model.addAttribute("totalProperties", dashboard.getListingsCount());
        return "landlord/properties";
    }

    @GetMapping("/landlord/bookings")
    @PreAuthorize("hasRole('LANDLORD')")
    public String landlordBookings(Model model) {
        UserEntity landlord = userService.getCurrentUserEntityOrThrow();
        LandlordDashboardResponse dashboard = dashboardService.getLandlordDashboard(landlord);
        model.addAttribute("loggedInUser", userService.getCurrentUserSummary());
        model.addAttribute("reservations", dashboard.getReservations());
        model.addAttribute("pendingApprovals", dashboard.getPendingReservations());
        return "landlord/bookings";
    }

    @PostMapping("/landlord/reservations/{reservationId}/approve")
    @PreAuthorize("hasRole('LANDLORD')")
    public String approveReservation(@PathVariable Long reservationId, RedirectAttributes redirectAttributes) {
        reservationService.approveReservation(reservationId, userService.getCurrentUserEntityOrThrow());
        redirectAttributes.addFlashAttribute("successMessage", "Reservation approved.");
        return "redirect:/landlord/bookings";
    }

    @PostMapping({"/landlord/reservations/{id}/reject", "/bookings/reject/{id}"})
    @PreAuthorize("hasRole('LANDLORD')")
    public String rejectReservation(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        reservationService.rejectReservation(id, userService.getCurrentUserEntityOrThrow());
        redirectAttributes.addFlashAttribute("successMessage", "Reservation rejected.");
        return "redirect:/landlord/bookings";
    }

    private void populateTenantDashboardModel(
            Model model,
            UserSummaryResponse loggedInUser,
            TenantDashboardResponse dashboard) {
        long pendingBookings = dashboard.getReservations().stream()
                .filter(reservation -> reservation.getStatus() == ReservationStatus.PENDING)
                .count();
        long paidBookings = dashboard.getReservations().stream()
                .filter(reservation -> reservation.getStatus() == ReservationStatus.APPROVED && reservation.isPaid())
                .count();
        long paymentDueBookings = dashboard.getReservations().stream()
                .filter(reservation -> reservation.getStatus() == ReservationStatus.APPROVED && !reservation.isPaid())
                .count();
        model.addAttribute("loggedInUser", loggedInUser);
        model.addAttribute("dashboard", dashboard);
        model.addAttribute("totalBookings", dashboard.getReservationsCount());
        model.addAttribute("totalSpending", dashboard.getTotalSpend());
        model.addAttribute("approvedBookings", dashboard.getApprovedReservations());
        model.addAttribute("pendingBookings", pendingBookings);
        model.addAttribute("cancelledBookings", dashboard.getCancelledReservations());
        model.addAttribute("upcomingBookings", dashboard.getUpcomingReservations());
        model.addAttribute("availableListings", dashboard.getAvailableListings().size());
        model.addAttribute("paidBookings", paidBookings);
        model.addAttribute("paymentDueBookings", paymentDueBookings);
    }

    private void populateLandlordDashboardModel(
            Model model,
            UserSummaryResponse loggedInUser,
            LandlordDashboardResponse dashboard) {
        long rejectedReservations = dashboard.getReservations().stream()
                .filter(reservation -> reservation.getStatus() == ReservationStatus.REJECTED)
                .count();
        long paidBookings = dashboard.getReservations().stream()
                .filter(reservation -> reservation.getStatus() == ReservationStatus.APPROVED && reservation.isPaid())
                .count();
        long outstandingPayments = dashboard.getReservations().stream()
                .filter(reservation -> reservation.getStatus() == ReservationStatus.APPROVED && !reservation.isPaid())
                .count();
        model.addAttribute("loggedInUser", loggedInUser);
        model.addAttribute("dashboard", dashboard);
        model.addAttribute("totalProperties", dashboard.getListingsCount());
        model.addAttribute("totalBookings", dashboard.getTotalBookings());
        model.addAttribute("totalBookingsReceived", dashboard.getTotalBookings());
        model.addAttribute("totalRevenue", dashboard.getTotalRevenue());
        model.addAttribute("pendingApprovals", dashboard.getPendingReservations());
        model.addAttribute("approvedReservations", dashboard.getApprovedReservations());
        model.addAttribute("rejectedReservations", rejectedReservations);
        model.addAttribute("paidBookings", paidBookings);
        model.addAttribute("outstandingPayments", outstandingPayments);
    }
}
