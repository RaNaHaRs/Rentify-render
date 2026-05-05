package com.harsh.rentify.mapper;

import com.harsh.rentify.dto.request.UpdateProfileRequest;
import com.harsh.rentify.dto.response.LocationViewResponse;
import com.harsh.rentify.dto.response.ProfileCommentResponse;
import com.harsh.rentify.dto.response.ProfileViewResponse;
import com.harsh.rentify.entity.LocationEntity;
import com.harsh.rentify.entity.ProfileCommentEntity;
import com.harsh.rentify.entity.ProfileEntity;
import org.springframework.stereotype.Component;

@Component
public class ProfileMapper {

    private final UserMapper userMapper;

    public ProfileMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public ProfileViewResponse toView(ProfileEntity profile, int listingsCount, int reservationsCount) {
        ProfileViewResponse response = new ProfileViewResponse();
        response.setUser(userMapper.toSummary(profile.getUser()));
        response.setFirstName(profile.getFirstName());
        response.setLastName(profile.getLastName());
        response.setBirthday(profile.getBirthday());
        response.setPhone(profile.getPhone());
        response.setAbout(profile.getAbout());
        response.setLocation(toLocation(profile.getLocation()));
        response.setListingsCount(listingsCount);
        response.setReservationsCount(reservationsCount);
        response.setComments(profile.getComments().stream().map(this::toComment).toList());
        return response;
    }

    public ProfileCommentResponse toComment(ProfileCommentEntity entity) {
        ProfileCommentResponse response = new ProfileCommentResponse();
        response.setId(entity.getId());
        response.setAuthorUsername(entity.getAuthor().getUsername());
        response.setAuthorDisplayName(userMapper.resolveDisplayName(entity.getAuthor()));
        response.setContent(entity.getContent());
        response.setCreatedAt(entity.getCreatedAt());
        return response;
    }

    public LocationViewResponse toLocation(LocationEntity entity) {
        if (entity == null) {
            return null;
        }
        LocationViewResponse response = new LocationViewResponse();
        response.setFormattedAddress(entity.getFormattedAddress());
        response.setLocality(entity.getLocality());
        response.setCountry(entity.getCountry());
        response.setPostalCode(entity.getPostalCode());
        return response;
    }

    public UpdateProfileRequest toUpdateRequest(ProfileEntity profile) {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFirstName(profile.getFirstName());
        request.setLastName(profile.getLastName());
        request.setBirthday(profile.getBirthday());
        request.setPhone(profile.getPhone());
        request.setAbout(profile.getAbout());
        if (profile.getLocation() != null) {
            request.setFormattedAddress(profile.getLocation().getFormattedAddress());
            request.setLocality(profile.getLocation().getLocality());
            request.setCountry(profile.getLocation().getCountry());
            request.setPostalCode(profile.getLocation().getPostalCode());
        }
        return request;
    }
}
