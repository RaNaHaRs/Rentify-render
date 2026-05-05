package com.harsh.rentify.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.harsh.rentify.dto.request.CreateRoomRequest;
import com.harsh.rentify.dto.request.RoomSearchRequest;
import com.harsh.rentify.dto.response.RoomCardResponse;
import com.harsh.rentify.dto.response.RoomDetailResponse;
import com.harsh.rentify.entity.LocationEntity;
import com.harsh.rentify.entity.PropertyImageEntity;
import com.harsh.rentify.entity.ReservationStatus;
import com.harsh.rentify.entity.Role;
import com.harsh.rentify.entity.RoomEntity;
import com.harsh.rentify.entity.RoomTypeEntity;
import com.harsh.rentify.entity.UserEntity;
import com.harsh.rentify.exception.BusinessException;
import com.harsh.rentify.exception.NotFoundException;
import com.harsh.rentify.mapper.RoomMapper;
import com.harsh.rentify.repository.AmenityRepository;
import com.harsh.rentify.repository.PaymentRepository;
import com.harsh.rentify.repository.ReservationRepository;
import com.harsh.rentify.repository.RoomRepository;
import com.harsh.rentify.repository.RoomTypeRepository;
import com.harsh.rentify.repository.RuleRepository;
import com.harsh.rentify.repository.TransportRepository;
import com.harsh.rentify.repository.UserRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.JoinType;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final AmenityRepository amenityRepository;
    private final RuleRepository ruleRepository;
    private final TransportRepository transportRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final RoomMapper roomMapper;
    private final EntityManager entityManager;
    private final Cloudinary cloudinary;

    public RoomService(
            RoomRepository roomRepository,
            RoomTypeRepository roomTypeRepository,
            AmenityRepository amenityRepository,
            RuleRepository ruleRepository,
            TransportRepository transportRepository,
            ReservationRepository reservationRepository,
            PaymentRepository paymentRepository,
            UserRepository userRepository,
            RoomMapper roomMapper,
            EntityManager entityManager,
            Cloudinary cloudinary) {
        this.roomRepository = roomRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.amenityRepository = amenityRepository;
        this.ruleRepository = ruleRepository;
        this.transportRepository = transportRepository;
        this.reservationRepository = reservationRepository;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.roomMapper = roomMapper;
        this.entityManager = entityManager;
        this.cloudinary = cloudinary;
    }

    @Transactional(readOnly = true)
    public List<RoomCardResponse> getFeaturedRooms() {
        return roomRepository.findTop6ByOrderByCreatedAtDesc().stream()
                .map(roomMapper::toCard)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RoomCardResponse> searchRooms(RoomSearchRequest request) {
        Specification<RoomEntity> specification = Specification.where(null);

        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            String keyword = "%" + request.getKeyword().trim().toLowerCase() + "%";
            specification = specification.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("title")), keyword),
                    cb.like(cb.lower(root.get("description")), keyword),
                    cb.like(cb.lower(root.get("neighborhood")), keyword),
                    cb.like(cb.lower(root.join("location", JoinType.LEFT).get("locality")), keyword),
                    cb.like(cb.lower(root.join("location", JoinType.LEFT).get("formattedAddress")), keyword)));
        }

        if (request.getLocality() != null && !request.getLocality().isBlank()) {
            String locality = "%" + request.getLocality().trim().toLowerCase() + "%";
            specification = specification.and((root, query, cb) -> cb
                    .like(cb.lower(root.join("location", JoinType.LEFT).get("locality")), locality));
        }

        if (request.getTypeSlug() != null && !request.getTypeSlug().isBlank()) {
            specification = specification.and((root, query, cb) -> cb
                    .equal(root.join("type", JoinType.INNER).get("slug"), request.getTypeSlug()));
        }

        if (request.getGuests() != null) {
            specification = specification
                    .and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("maxPeople"), request.getGuests()));
        }

        if (request.getMinPrice() != null) {
            specification = specification.and(
                    (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("overnightPrice"), request.getMinPrice()));
        }

        if (request.getMaxPrice() != null) {
            specification = specification
                    .and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("overnightPrice"), request.getMaxPrice()));
        }

        return roomRepository.findAll(specification, resolveSearchSort(request)).stream()
                .filter(room -> isAvailable(room, request.getCheckInDate(), request.getCheckOutDate()))
                .map(roomMapper::toCard)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BigDecimal> getPriceFilterOptions() {
        return roomRepository.findAll(Sort.by(Sort.Direction.ASC, "overnightPrice")).stream()
                .map(RoomEntity::getOvernightPrice)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RoomCardResponse> getAllListings() {
        return roomRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(roomMapper::toCard)
                .toList();
    }

    @Transactional(readOnly = true)
    public RoomDetailResponse getRoomDetail(Long roomId) {
        return roomMapper.toDetail(getRoomEntityOrThrow(roomId));
    }

    @Transactional(readOnly = true)
    public List<RoomCardResponse> getListingsForLandlord(UserEntity landlord) {
        return roomRepository.findByHostOrderByCreatedAtDesc(landlord).stream()
                .map(roomMapper::toCard)
                .toList();
    }

    @Transactional
    public RoomEntity createRoom(UserEntity landlord, CreateRoomRequest request, MultipartFile[] images) {
        if (landlord.getRole() != Role.LANDLORD) {
            throw new BusinessException("Only landlord accounts can create listings.");
        }
        if (!landlord.isHostConfirmed()) {
            throw new BusinessException("Your landlord account is pending approval.");
        }

        RoomTypeEntity roomType = roomTypeRepository.findById(request.getTypeId())
                .orElseThrow(() -> new NotFoundException("Selected room type was not found."));

        UserEntity managedLandlord = userRepository.findById(landlord.getId())
                .orElseThrow(() -> new NotFoundException("Landlord account was not found."));

        RoomEntity room = new RoomEntity();
        managedLandlord.addRoom(room);
        room.setType(roomType);
        room.setTitle(request.getTitle().trim());
        room.setDescription(request.getDescription().trim());
        room.setOvernightPrice(request.getOvernightPrice());
        room.setExtraPersonPrice(request.getExtraPersonPrice());
        room.setMaxPeople(request.getMaxPeople());
        room.setMinOvernights(request.getMinOvernights());
        room.setBeds(request.getBeds());
        room.setBathrooms(request.getBathrooms());
        room.setBedrooms(request.getBedrooms());
        room.setSquareMeters(request.getSquareMeters());
        room.setNeighborhood(blankToNull(request.getNeighborhood()));
        room.setHouseRulesText(blankToNull(request.getHouseRulesText()));
        storeImages(room, images);
        if (room.getImages().isEmpty()) {
            room.setPrimaryImageUrl(blankToNull(request.getPrimaryImageUrl()));
        }

        LocationEntity location = new LocationEntity();
        location.setFormattedAddress(request.getFormattedAddress().trim());
        location.setLocality(blankToNull(request.getLocality()));
        location.setCountry(blankToNull(request.getCountry()));
        location.setPostalCode(blankToNull(request.getPostalCode()));
        room.setLocation(location);

        room.setAmenities(request.getAmenityIds().isEmpty()
                ? new LinkedHashSet<>()
                : new LinkedHashSet<>(amenityRepository.findAllById(request.getAmenityIds())));
        room.setRules(request.getRuleIds().isEmpty()
                ? new LinkedHashSet<>()
                : new LinkedHashSet<>(ruleRepository.findAllById(request.getRuleIds())));
        room.setTransports(request.getTransportIds().isEmpty()
                ? new LinkedHashSet<>()
                : new LinkedHashSet<>(transportRepository.findAllById(request.getTransportIds())));

        return roomRepository.save(room);
    }

    @Transactional
    public void deleteProperty(Long propertyId, Long userId) {
        RoomEntity property = roomRepository.findById(propertyId)
                .orElseThrow(() -> new NotFoundException("Property with id " + propertyId + " was not found."));
        if (!property.getHost().getId().equals(userId)) {
            throw new BusinessException("You can delete only your own properties.");
        }

        deleteManagedProperty(property);
    }

    @Transactional
    public void deletePropertyByAdmin(Long propertyId) {
        RoomEntity property = roomRepository.findById(propertyId)
                .orElseThrow(() -> new NotFoundException("Property with id " + propertyId + " was not found."));
        deleteManagedProperty(property);
    }

    private void deleteManagedProperty(RoomEntity property) {
        paymentRepository.deleteByBookingRoomId(property.getId());
        property.getHost().removeRoom(property);
        roomRepository.flush();
        entityManager.clear();
    }

    @Transactional(readOnly = true)
    public RoomEntity getRoomEntityOrThrow(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("Room with id " + roomId + " was not found."));
    }

    @Transactional(readOnly = true)
    public String resolveStoredOrExternalImage(RoomEntity room) {
        if (!room.getImages().isEmpty()) {
            return room.getImages().get(0).getImageUrl();
        }
        if (room.getPrimaryImageUrl() != null && !room.getPrimaryImageUrl().isBlank()) {
            return room.getPrimaryImageUrl();
        }
        return "/images/placeholder-room.svg";
    }

    @Transactional(readOnly = true)
    public boolean isAvailable(RoomEntity room, LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            return true;
        }
        return !reservationRepository.existsConflictingReservation(
                room.getId(),
                EnumSet.of(ReservationStatus.PENDING, ReservationStatus.APPROVED),
                checkIn,
                checkOut);
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private Sort resolveSearchSort(RoomSearchRequest request) {
        String sortKey = request.getSort();
        if (sortKey == null || sortKey.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        return switch (sortKey) {
            case "priceAsc" -> Sort.by(Sort.Direction.ASC, "overnightPrice");
            case "priceDesc" -> Sort.by(Sort.Direction.DESC, "overnightPrice");
            case "newest" -> Sort.by(Sort.Direction.DESC, "createdAt");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    private void storeImages(RoomEntity room, MultipartFile[] images) {
        List<MultipartFile> files = Arrays.stream(images == null ? new MultipartFile[0] : images)
                .filter(Objects::nonNull)
                .filter(file -> !file.isEmpty())
                .toList();

        if (files.isEmpty()) {
            room.setImage(null);
            room.setImageContentType(null);
            return;
        }

        room.getImages().clear();
        room.setImage(null);
        room.setImageContentType(null);
        room.setPrimaryImageUrl(null);

        for (MultipartFile file : files) {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new BusinessException("Please upload valid image files only.");
            }

            try {
                // Upload to Cloudinary
                Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "rentify/properties",
                    "public_id", UUID.randomUUID().toString(),
                    "resource_type", "image"
                ));

                String imageUrl = (String) uploadResult.get("secure_url");

                PropertyImageEntity propertyImage = new PropertyImageEntity();
                propertyImage.setImageUrl(imageUrl);
                room.addImage(propertyImage);

            } catch (IOException exception) {
                throw new BusinessException("Listing images could not be uploaded to cloud storage.");
            }
        }

        if (!room.getImages().isEmpty()) {
            room.setPrimaryImageUrl(room.getImages().get(0).getImageUrl());
        }
    }
}
