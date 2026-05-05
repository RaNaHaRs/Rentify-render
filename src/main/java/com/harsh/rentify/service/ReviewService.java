package com.harsh.rentify.service;

import com.harsh.rentify.dto.request.ReviewRequest;
import com.harsh.rentify.entity.ReviewEntity;
import com.harsh.rentify.entity.Role;
import com.harsh.rentify.entity.RoomEntity;
import com.harsh.rentify.entity.UserEntity;
import com.harsh.rentify.exception.BusinessException;
import com.harsh.rentify.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RoomService roomService;
    private final ReservationService reservationService;

    public ReviewService(
            ReviewRepository reviewRepository,
            RoomService roomService,
            ReservationService reservationService
    ) {
        this.reviewRepository = reviewRepository;
        this.roomService = roomService;
        this.reservationService = reservationService;
    }

    @Transactional
    public void addReview(Long roomId, ReviewRequest request, UserEntity author) {
        if (author.getRole() != Role.TENANT) {
            throw new BusinessException("Only tenants can publish reviews.");
        }

        RoomEntity room = roomService.getRoomEntityOrThrow(roomId);
        if (!reservationService.hasApprovedReservation(author, room)) {
            throw new BusinessException("Only tenants with an approved stay can review this property.");
        }
        if (reviewRepository.existsByRoomAndAuthor(room, author)) {
            throw new BusinessException("You have already reviewed this property.");
        }

        ReviewEntity review = new ReviewEntity();
        review.setRoom(room);
        review.setAuthor(author);
        review.setRating(request.getRating());
        review.setComment(request.getComment().trim());
        reviewRepository.save(review);
    }
}
