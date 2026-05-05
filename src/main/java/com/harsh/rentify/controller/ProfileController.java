package com.harsh.rentify.controller;

import com.harsh.rentify.dto.request.CommentRequest;
import com.harsh.rentify.dto.request.UpdateProfileRequest;
import com.harsh.rentify.service.UserService;
import com.harsh.rentify.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profiles/{username}")
    public String viewProfile(@PathVariable String username, Model model) {
        if (!model.containsAttribute("commentRequest")) {
            model.addAttribute("commentRequest", new CommentRequest());
        }
        model.addAttribute("profile", userService.getProfileView(username));
        model.addAttribute("isOwnProfile", username.equals(SecurityUtils.getCurrentUsername()));
        return "profile/view";
    }

    @GetMapping("/account/profile")
    @PreAuthorize("isAuthenticated()")
    public String editProfile(Model model) {
        model.addAttribute("updateProfileRequest", userService.getProfileForm(userService.getCurrentUserEntityOrThrow()));
        return "profile/edit";
    }

    @PostMapping("/account/profile")
    @PreAuthorize("isAuthenticated()")
    public String updateProfile(
            @Valid @ModelAttribute("updateProfileRequest") UpdateProfileRequest updateProfileRequest,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "profile/edit";
        }
        userService.updateProfile(userService.getCurrentUserEntityOrThrow(), updateProfileRequest);
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated.");
        return "redirect:/profiles/" + userService.getCurrentUserEntityOrThrow().getUsername();
    }

    @PostMapping("/profiles/{username}/comments")
    @PreAuthorize("isAuthenticated()")
    public String commentOnProfile(
            @PathVariable String username,
            @Valid @ModelAttribute("commentRequest") CommentRequest commentRequest,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return viewProfile(username, model);
        }
        userService.addProfileComment(username, commentRequest, userService.getCurrentUserEntityOrThrow());
        redirectAttributes.addFlashAttribute("successMessage", "Comment posted.");
        return "redirect:/profiles/" + username;
    }
}
