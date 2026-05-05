package com.harsh.rentify.service;

import com.harsh.rentify.dto.request.CommentRequest;
import com.harsh.rentify.dto.request.UpdateProfileRequest;
import com.harsh.rentify.dto.response.ProfileViewResponse;
import com.harsh.rentify.dto.response.UserSummaryResponse;
import com.harsh.rentify.entity.LocationEntity;
import com.harsh.rentify.entity.ProfileCommentEntity;
import com.harsh.rentify.entity.ProfileEntity;
import com.harsh.rentify.entity.Role;
import com.harsh.rentify.entity.UserEntity;
import com.harsh.rentify.exception.NotFoundException;
import com.harsh.rentify.mapper.ProfileMapper;
import com.harsh.rentify.mapper.UserMapper;
import com.harsh.rentify.repository.ProfileCommentRepository;
import com.harsh.rentify.repository.ProfileRepository;
import com.harsh.rentify.repository.ReservationRepository;
import com.harsh.rentify.repository.RoomRepository;
import com.harsh.rentify.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final ProfileCommentRepository profileCommentRepository;
    private final UserMapper userMapper;
    private final ProfileMapper profileMapper;

    public UserService(
            UserRepository userRepository,
            ProfileRepository profileRepository,
            RoomRepository roomRepository,
            ReservationRepository reservationRepository,
            ProfileCommentRepository profileCommentRepository,
            UserMapper userMapper,
            ProfileMapper profileMapper
    ) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
        this.profileCommentRepository = profileCommentRepository;
        this.userMapper = userMapper;
        this.profileMapper = profileMapper;
    }

    @Transactional(readOnly = true)
    public Optional<UserEntity> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public UserEntity getByUsernameOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User '" + username + "' was not found."));
    }

    @Transactional(readOnly = true)
    public ProfileViewResponse getProfileView(String username) {
        ProfileEntity profile = profileRepository.findByUserUsername(username)
                .orElseThrow(() -> new NotFoundException("Profile for '" + username + "' was not found."));
        int listingsCount = profile.getUser().getRooms().size();
        int reservationsCount = profile.getUser().getReservations().size();
        return profileMapper.toView(profile, listingsCount, reservationsCount);
    }

    @Transactional(readOnly = true)
    public UpdateProfileRequest getProfileForm(UserEntity currentUser) {
        ProfileEntity profile = profileRepository.findByUser(currentUser)
                .orElseThrow(() -> new NotFoundException("Current user profile was not found."));
        return profileMapper.toUpdateRequest(profile);
    }

    @Transactional
    public void updateProfile(UserEntity currentUser, UpdateProfileRequest request) {
        ProfileEntity profile = profileRepository.findByUser(currentUser)
                .orElseThrow(() -> new NotFoundException("Current user profile was not found."));
        profile.setFirstName(request.getFirstName().trim());
        profile.setLastName(request.getLastName().trim());
        profile.setBirthday(request.getBirthday());
        profile.setPhone(blankToNull(request.getPhone()));
        profile.setAbout(blankToNull(request.getAbout()));

        LocationEntity location = profile.getLocation();
        if (location == null) {
            location = new LocationEntity();
            profile.setLocation(location);
        }
        location.setFormattedAddress(blankToNull(request.getFormattedAddress()));
        location.setLocality(blankToNull(request.getLocality()));
        location.setCountry(blankToNull(request.getCountry()));
        location.setPostalCode(blankToNull(request.getPostalCode()));
    }

    @Transactional
    public void addProfileComment(String username, CommentRequest request, UserEntity author) {
        ProfileEntity profile = profileRepository.findByUserUsername(username)
                .orElseThrow(() -> new NotFoundException("Profile for '" + username + "' was not found."));
        ProfileCommentEntity comment = new ProfileCommentEntity();
        comment.setProfile(profile);
        comment.setAuthor(author);
        comment.setContent(request.getContent().trim());
        profileCommentRepository.save(comment);
    }

    @Transactional(readOnly = true)
    public List<UserSummaryResponse> getPendingLandlords() {
        return userRepository.findByRoleAndHostConfirmedFalseOrderByCreatedAtAsc(Role.LANDLORD).stream()
                .map(userMapper::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserSummaryResponse> getUsersByRole(Role role) {
        return userRepository.findByRoleOrderByCreatedAtDesc(role).stream()
                .map(userMapper::toSummary)
                .toList();
    }

    @Transactional
    public void confirmLandlord(Long userId) {
        UserEntity landlord = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Landlord account was not found."));
        landlord.setHostConfirmed(true);
    }

    @Transactional(readOnly = true)
    public UserEntity getCurrentUserEntityOrThrow() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new NotFoundException("No authenticated user is available.");
        }
        return getByUsernameOrThrow(authentication.getName());
    }

    @Transactional(readOnly = true)
    public UserSummaryResponse getCurrentUserSummary() {
        return userMapper.toSummary(getCurrentUserEntityOrThrow());
    }

    @Transactional(readOnly = true)
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    @Transactional(readOnly = true)
    public long countUsers() {
        return userRepository.count();
    }

    @Transactional(readOnly = true)
    public long countUsersByRole(Role role) {
        return userRepository.countByRole(role);
    }

    @Transactional(readOnly = true)
    public long countPendingLandlords() {
        return userRepository.findByRoleAndHostConfirmedFalseOrderByCreatedAtAsc(Role.LANDLORD).size();
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
