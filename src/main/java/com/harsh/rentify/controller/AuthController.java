package com.harsh.rentify.controller;

import com.harsh.rentify.dto.request.RegisterRequest;
import com.harsh.rentify.entity.Role;
import com.harsh.rentify.service.AuthService;
import com.harsh.rentify.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String login() {
        if (SecurityUtils.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String register(@ModelAttribute("registerRequest") RegisterRequest registerRequest) {
        if (SecurityUtils.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registerRequest") RegisterRequest registerRequest,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        authService.register(registerRequest);
        redirectAttributes.addFlashAttribute("successMessage", "Account created. You can sign in now.");
        return "redirect:/login?registered=true";
    }

    @ModelAttribute("registrationRoles")
    public Role[] registrationRoles() {
        return new Role[]{Role.LANDLORD, Role.TENANT};
    }
}
