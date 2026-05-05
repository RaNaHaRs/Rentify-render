package com.harsh.rentify.controller;

import com.harsh.rentify.dto.response.PaymentViewResponse;
import com.harsh.rentify.service.PaymentService;
import com.harsh.rentify.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PaymentController {

    private final PaymentService paymentService;
    private final UserService userService;

    public PaymentController(PaymentService paymentService, UserService userService) {
        this.paymentService = paymentService;
        this.userService = userService;
    }

    @GetMapping("/payments/{bookingId}")
    @PreAuthorize("hasRole('TENANT')")
    public String paymentPage(@PathVariable Long bookingId, Model model) {
        PaymentViewResponse payment = paymentService.getPaymentView(bookingId, userService.getCurrentUserEntityOrThrow());
        model.addAttribute("payment", payment);
        return "payments/payment";
    }

    @PostMapping("/payments")
    @PreAuthorize("hasRole('TENANT')")
    public String processPayment(@RequestParam("bookingId") Long bookingId, RedirectAttributes redirectAttributes) {
        paymentService.processPayment(bookingId, userService.getCurrentUserEntityOrThrow());
        redirectAttributes.addFlashAttribute("successMessage", "Payment successful!");
        return "redirect:/tenant/payments";
    }
}
