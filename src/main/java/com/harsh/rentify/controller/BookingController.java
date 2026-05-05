package com.harsh.rentify.controller;

import com.harsh.rentify.service.BookingService;
import com.harsh.rentify.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class BookingController {

    private final BookingService bookingService;
    private final UserService userService;

    public BookingController(BookingService bookingService, UserService userService) {
        this.bookingService = bookingService;
        this.userService = userService;
    }

    @PostMapping("/bookings/cancel/{id}")
    @PreAuthorize("hasRole('TENANT')")
    public String cancelBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        bookingService.cancelBooking(id, userService.getCurrentUserEntityOrThrow());
        redirectAttributes.addFlashAttribute("successMessage", "Booking cancelled successfully");
        return "redirect:/tenant/bookings";
    }
}
