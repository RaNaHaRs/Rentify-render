package com.harsh.rentify.config;

import com.harsh.rentify.service.NotificationService;
import com.harsh.rentify.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    private final UserService userService;
    private final NotificationService notificationService;

    @Value("${app.name}")
    private String appName;

    public GlobalModelAttributes(UserService userService, NotificationService notificationService) {
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        model.addAttribute("appName", appName);
        if (userService.isAuthenticated()) {
            var currentUser = userService.getCurrentUserSummary();
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("loggedInUser", currentUser);
            var notifications = notificationService.getNotificationsFor(userService.getCurrentUserEntityOrThrow());
            model.addAttribute("notifications", notifications);
            model.addAttribute("notificationCount", notifications.size());
        }
    }
}
