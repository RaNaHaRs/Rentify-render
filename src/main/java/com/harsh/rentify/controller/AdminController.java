package com.harsh.rentify.controller;

import com.harsh.rentify.dto.response.AdminDashboardResponse;
import com.harsh.rentify.service.DashboardService;
import com.harsh.rentify.service.ReservationService;
import com.harsh.rentify.service.RoomService;
import com.harsh.rentify.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminController {

    private final DashboardService dashboardService;
    private final RoomService roomService;
    private final ReservationService reservationService;
    private final UserService userService;

    public AdminController(
            DashboardService dashboardService,
            RoomService roomService,
            ReservationService reservationService,
            UserService userService
    ) {
        this.dashboardService = dashboardService;
        this.roomService = roomService;
        this.reservationService = reservationService;
        this.userService = userService;
    }

    @GetMapping({"/admin/dashboard", "/admin-dashboard"})
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDashboard(Model model) {
        AdminDashboardResponse dashboard = dashboardService.getAdminDashboard();
        model.addAttribute("loggedInUser", userService.getCurrentUserSummary());
        model.addAttribute("dashboard", dashboard);
        model.addAttribute("totalUsers", dashboard.getTotalUsers());
        model.addAttribute("totalLandlords", dashboard.getTotalLandlords());
        model.addAttribute("totalTenants", dashboard.getTotalTenants());
        model.addAttribute("totalAdmins", dashboard.getTotalAdmins());
        model.addAttribute("totalProperties", dashboard.getTotalRooms());
        model.addAttribute("totalBookings", dashboard.getTotalReservations());
        model.addAttribute("totalRevenue", dashboard.getTotalRevenue());
        model.addAttribute("pendingLandlords", dashboard.getPendingLandlords());
        model.addAttribute("revenueTrendLabels", dashboard.getRevenueTrendLabels());
        model.addAttribute("revenueTrendValues", dashboard.getRevenueTrendValues());
        return "dashboard/admin";
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminUsers(Model model) {
        AdminDashboardResponse dashboard = dashboardService.getAdminDashboard();
        model.addAttribute("loggedInUser", userService.getCurrentUserSummary());
        model.addAttribute("dashboard", dashboard);
        return "admin/users";
    }

    @GetMapping("/admin/properties")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminProperties(Model model) {
        model.addAttribute("loggedInUser", userService.getCurrentUserSummary());
        model.addAttribute("properties", roomService.getAllListings());
        return "admin/properties";
    }

    @GetMapping("/admin/bookings")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminBookings(Model model) {
        model.addAttribute("loggedInUser", userService.getCurrentUserSummary());
        model.addAttribute("reservations", reservationService.getAllReservations());
        return "admin/bookings";
    }

    @PostMapping("/admin/landlords/{userId}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public String confirmLandlord(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        userService.confirmLandlord(userId);
        redirectAttributes.addFlashAttribute("successMessage", "Landlord account approved.");
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/admin/properties/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteProperty(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        roomService.deletePropertyByAdmin(id);
        redirectAttributes.addFlashAttribute("successMessage", "Property deleted by admin");
        return "redirect:/admin/properties";
    }
}
