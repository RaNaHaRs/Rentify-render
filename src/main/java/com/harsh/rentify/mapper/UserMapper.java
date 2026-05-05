package com.harsh.rentify.mapper;

import com.harsh.rentify.dto.response.UserSummaryResponse;
import com.harsh.rentify.entity.ProfileEntity;
import com.harsh.rentify.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserSummaryResponse toSummary(UserEntity user) {
        UserSummaryResponse response = new UserSummaryResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setDisplayName(resolveDisplayName(user));
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setHostConfirmed(user.isHostConfirmed());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }

    public String resolveDisplayName(UserEntity user) {
        ProfileEntity profile = user.getProfile();
        if (profile == null) {
            return user.getUsername();
        }

        String fullName = (profile.getFirstName() + " " + profile.getLastName()).trim();
        return fullName.isBlank() ? user.getUsername() : fullName;
    }
}
