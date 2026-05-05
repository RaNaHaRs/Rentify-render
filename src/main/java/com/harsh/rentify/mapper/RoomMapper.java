package com.harsh.rentify.mapper;

import com.harsh.rentify.dto.response.ReservationViewResponse;
import com.harsh.rentify.dto.response.ReviewViewResponse;
import com.harsh.rentify.dto.response.RoomCardResponse;
import com.harsh.rentify.dto.response.RoomDetailResponse;
import com.harsh.rentify.entity.LocationEntity;
import com.harsh.rentify.entity.PropertyImageEntity;
import com.harsh.rentify.entity.ReservationEntity;
import com.harsh.rentify.entity.ReviewEntity;
import com.harsh.rentify.entity.RoomEntity;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RoomMapper {

    private final UserMapper userMapper;

    public RoomMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public RoomCardResponse toCard(RoomEntity room) {
        RoomCardResponse response = new RoomCardResponse();
        response.setId(room.getId());
        response.setTitle(room.getTitle());
        response.setType(room.getType().getName());
        response.setLocation(formatLocation(room.getLocation()));
        response.setOvernightPrice(room.getOvernightPrice());
        response.setMaxPeople(room.getMaxPeople());
        response.setAverageRating(averageRating(room));
        response.setReviewCount(room.getReviews().size());
        response.setImageUrl(resolveImage(room));
        response.setHostUsername(room.getHost().getUsername());
        response.setHostDisplayName(userMapper.resolveDisplayName(room.getHost()));
        response.setListingStatus(room.getHost().isHostConfirmed() ? "ACTIVE" : "PENDING");
        return response;
    }

    public RoomDetailResponse toDetail(RoomEntity room) {
        RoomDetailResponse response = new RoomDetailResponse();
        response.setId(room.getId());
        response.setTitle(room.getTitle());
        response.setType(room.getType().getName());
        response.setDescription(room.getDescription());
        response.setLocation(formatLocation(room.getLocation()));
        response.setNeighborhood(room.getNeighborhood());
        response.setImageUrl(resolveImage(room));
        response.setImageUrls(resolveImages(room));
        response.setOvernightPrice(room.getOvernightPrice());
        response.setExtraPersonPrice(room.getExtraPersonPrice());
        response.setMaxPeople(room.getMaxPeople());
        response.setMinOvernights(room.getMinOvernights());
        response.setBeds(room.getBeds());
        response.setBathrooms(room.getBathrooms());
        response.setBedrooms(room.getBedrooms());
        response.setSquareMeters(room.getSquareMeters());
        response.setAverageRating(averageRating(room));
        response.setReviewCount(room.getReviews().size());
        response.setHouseRulesText(room.getHouseRulesText());
        response.setHost(userMapper.toSummary(room.getHost()));
        response.setAmenities(room.getAmenities().stream().map(a -> a.getName()).sorted().toList());
        response.setRules(room.getRules().stream().map(r -> r.getName()).sorted().toList());
        response.setTransports(room.getTransports().stream().map(t -> t.getName()).sorted().toList());
        response.setReviews(room.getReviews().stream()
                .sorted(Comparator.comparing(ReviewEntity::getCreatedAt).reversed())
                .map(this::toReview)
                .toList());
        return response;
    }

    public ReviewViewResponse toReview(ReviewEntity entity) {
        ReviewViewResponse response = new ReviewViewResponse();
        response.setId(entity.getId());
        response.setAuthorUsername(entity.getAuthor().getUsername());
        response.setAuthorDisplayName(userMapper.resolveDisplayName(entity.getAuthor()));
        response.setRating(entity.getRating());
        response.setComment(entity.getComment());
        response.setCreatedAt(entity.getCreatedAt());
        return response;
    }

    public ReservationViewResponse toReservation(ReservationEntity entity) {
        ReservationViewResponse response = new ReservationViewResponse();
        response.setId(entity.getId());
        response.setRoomId(entity.getRoom().getId());
        response.setRoomTitle(entity.getRoom().getTitle());
        response.setRoomLocation(formatLocation(entity.getRoom().getLocation()));
        response.setTenantUsername(entity.getTenant().getUsername());
        response.setTenantDisplayName(userMapper.resolveDisplayName(entity.getTenant()));
        response.setLandlordDisplayName(userMapper.resolveDisplayName(entity.getRoom().getHost()));
        response.setCheckInDate(entity.getCheckInDate());
        response.setCheckOutDate(entity.getCheckOutDate());
        response.setGuests(entity.getGuests());
        response.setTotalCost(entity.getTotalCost());
        response.setStatus(entity.getStatus());
        response.setPaid(entity.isPaid());
        response.setRejectReason(entity.getRejectReason());
        response.setCreatedAt(entity.getCreatedAt());
        return response;
    }

    public String formatLocation(LocationEntity location) {
        if (location == null) {
            return "Location unavailable";
        }
        if (location.getFormattedAddress() != null && !location.getFormattedAddress().isBlank()) {
            return location.getFormattedAddress();
        }
        String locality = location.getLocality() == null ? "" : location.getLocality();
        String country = location.getCountry() == null ? "" : location.getCountry();
        String value = (locality + ", " + country).replaceAll("^, |, $", "").trim();
        return value.isBlank() ? "Location unavailable" : value;
    }

    public Double averageRating(RoomEntity room) {
        if (room.getReviews().isEmpty()) {
            return 0.0;
        }
        return room.getReviews().stream()
                .mapToInt(ReviewEntity::getRating)
                .average()
                .orElse(0.0);
    }

    public String resolveImage(RoomEntity room) {
        return resolveImages(room).get(0);
    }

    public List<String> resolveImages(RoomEntity room) {
        List<String> uploadedImages = room.getImages().stream()
                .map(PropertyImageEntity::getImageUrl)
                .filter(imageUrl -> imageUrl != null && !imageUrl.isBlank())
                .toList();
        if (!uploadedImages.isEmpty()) {
            return uploadedImages;
        }
        if (room.getImage() != null && room.getImage().length > 0) {
            return List.of("/rooms/" + room.getId() + "/image");
        }
        if (room.getPrimaryImageUrl() == null || room.getPrimaryImageUrl().isBlank()) {
            return List.of("/images/placeholder-room.svg");
        }
        return List.of(room.getPrimaryImageUrl());
    }
}
