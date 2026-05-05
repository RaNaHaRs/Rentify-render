package com.harsh.rentify.controller;

import com.harsh.rentify.dto.request.RoomSearchRequest;
import com.harsh.rentify.entity.Role;
import com.harsh.rentify.entity.UserEntity;
import com.harsh.rentify.service.ReferenceDataService;
import com.harsh.rentify.service.RoomService;
import com.harsh.rentify.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    private final RoomService roomService;
    private final ReferenceDataService referenceDataService;
    private final UserService userService;

    public HomeController(
            RoomService roomService,
            ReferenceDataService referenceDataService,
            UserService userService
    ) {
        this.roomService = roomService;
        this.referenceDataService = referenceDataService;
        this.userService = userService;
    }

    @GetMapping("/")
    public String home(@ModelAttribute("search") RoomSearchRequest search, Model model) {
        model.addAttribute("featuredRooms", roomService.getFeaturedRooms());
        model.addAttribute("roomTypes", referenceDataService.getRoomTypes());
        return "index";
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(value = "loginSuccess", required = false) Boolean loginSuccess) {
        UserEntity currentUser = userService.getCurrentUserEntityOrThrow();
        String loginSuccessSuffix = Boolean.TRUE.equals(loginSuccess) ? "?loginSuccess=true" : "";
        if (currentUser.getRole() == Role.ADMIN) {
            return "redirect:/admin/dashboard" + loginSuccessSuffix;
        }
        if (currentUser.getRole() == Role.LANDLORD) {
            return "redirect:/landlord/dashboard" + loginSuccessSuffix;
        }
        return "redirect:/tenant/dashboard" + loginSuccessSuffix;
    }
}
