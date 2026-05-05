package com.harsh.rentify.controller;

import com.harsh.rentify.dto.request.BookingRequest;
import com.harsh.rentify.dto.request.CreateRoomRequest;
import com.harsh.rentify.dto.request.ReviewRequest;
import com.harsh.rentify.dto.request.RoomSearchRequest;
import com.harsh.rentify.entity.Role;
import com.harsh.rentify.exception.BusinessException;
import com.harsh.rentify.exception.NotFoundException;
import com.harsh.rentify.service.ReferenceDataService;
import com.harsh.rentify.service.ReservationService;
import com.harsh.rentify.service.ReviewService;
import com.harsh.rentify.service.RoomService;
import com.harsh.rentify.service.UserService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class RoomController {

    private final RoomService roomService;
    private final ReservationService reservationService;
    private final ReviewService reviewService;
    private final ReferenceDataService referenceDataService;
    private final UserService userService;

    public RoomController(
            RoomService roomService,
            ReservationService reservationService,
            ReviewService reviewService,
            ReferenceDataService referenceDataService,
            UserService userService) {
        this.roomService = roomService;
        this.reservationService = reservationService;
        this.reviewService = reviewService;
        this.referenceDataService = referenceDataService;
        this.userService = userService;
    }

    @GetMapping("/rooms")
    public String rooms(@ModelAttribute("search") RoomSearchRequest search, Model model) {
        model.addAttribute("rooms", roomService.searchRooms(search));
        model.addAttribute("roomTypes", referenceDataService.getRoomTypes());
        return "rooms/list";
    }

    @GetMapping("/rooms/{id}")
    public String roomDetail(@PathVariable Long id, Model model) {
        if (!model.containsAttribute("bookingRequest")) {
            model.addAttribute("bookingRequest", new BookingRequest());
        }
        if (!model.containsAttribute("reviewRequest")) {
            model.addAttribute("reviewRequest", new ReviewRequest());
        }

        model.addAttribute("room", roomService.getRoomDetail(id));
        model.addAttribute("canBook", userService.isAuthenticated()
                && userService.getCurrentUserEntityOrThrow().getRole() == Role.TENANT);
        model.addAttribute("canReview", userService.isAuthenticated()
                && userService.getCurrentUserEntityOrThrow().getRole() == Role.TENANT);
        return "rooms/detail";
    }

    @PostMapping("/rooms/{id}/book")
    @PreAuthorize("hasRole('TENANT')")
    public String bookRoom(
            @PathVariable Long id,
            @Valid @ModelAttribute("bookingRequest") BookingRequest bookingRequest,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("reviewRequest", new ReviewRequest());
            return roomDetail(id, model);
        }
        reservationService.createReservation(id, bookingRequest, userService.getCurrentUserEntityOrThrow());
        redirectAttributes.addFlashAttribute("successMessage", "Booking request submitted.");
        return "redirect:/rooms/" + id;
    }

    @PostMapping("/rooms/{id}/reviews")
    @PreAuthorize("hasRole('TENANT')")
    public String reviewRoom(
            @PathVariable Long id,
            @Valid @ModelAttribute("reviewRequest") ReviewRequest reviewRequest,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("bookingRequest", new BookingRequest());
            return roomDetail(id, model);
        }
        reviewService.addReview(id, reviewRequest, userService.getCurrentUserEntityOrThrow());
        redirectAttributes.addFlashAttribute("successMessage", "Review published.");
        return "redirect:/rooms/" + id;
    }

    @GetMapping({ "/landlord/listings/new", "/landlord/create-property", "/add-property" })
    @PreAuthorize("hasRole('LANDLORD')")
    public String newListing(@ModelAttribute("propertyRequest") CreateRoomRequest propertyRequest, Model model) {
        populateListingReferences(model);
        return "rooms/form";
    }

    @PostMapping({ "/landlord/listings", "/add-property" })
    @PreAuthorize("hasRole('LANDLORD')")
    public String createListing(
            @Valid @ModelAttribute("propertyRequest") CreateRoomRequest propertyRequest,
            BindingResult bindingResult,
            @RequestParam(value = "images", required = false) MultipartFile[] images,
            @RequestParam(value = "image", required = false) MultipartFile image,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populateListingReferences(model);
            model.addAttribute("formError",
                    "Listing was not created. Please fix the validation errors and submit again.");
            return "rooms/form";
        }
        try {
            roomService.createRoom(
                    userService.getCurrentUserEntityOrThrow(),
                    propertyRequest,
                    mergeImages(images, image));
        } catch (BusinessException exception) {
            populateListingReferences(model);
            model.addAttribute("formError", exception.getMessage());
            return "rooms/form";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Listing added successfully!");
        return "redirect:/landlord/properties";
    }

    @PostMapping("/properties/delete/{id}")
    @PreAuthorize("hasRole('LANDLORD')")
    public String deleteProperty(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        roomService.deleteProperty(id, userService.getCurrentUserEntityOrThrow().getId());
        redirectAttributes.addFlashAttribute("successMessage", "Property deleted successfully.");
        return "redirect:/landlord/properties";
    }

    private void populateListingReferences(Model model) {
        model.addAttribute("roomTypes", referenceDataService.getRoomTypes());
        model.addAttribute("amenities", referenceDataService.getAmenities());
        model.addAttribute("rules", referenceDataService.getRules());
        model.addAttribute("transports", referenceDataService.getTransports());
    }

    private MediaType parseMediaType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        try {
            return MediaType.parseMediaType(contentType);
        } catch (InvalidMediaTypeException exception) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    private MultipartFile[] mergeImages(MultipartFile[] images, MultipartFile image) {
        List<MultipartFile> uploads = new ArrayList<>();
        if (images != null) {
            for (MultipartFile file : images) {
                if (file != null) {
                    uploads.add(file);
                }
            }
        }
        if (image != null) {
            uploads.add(image);
        }
        return uploads.toArray(MultipartFile[]::new);
    }
}
