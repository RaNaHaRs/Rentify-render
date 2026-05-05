package com.harsh.rentify.dto.response;

import com.harsh.rentify.entity.Role;
import java.time.LocalDateTime;

public class UserSummaryResponse {

    private Long id;
    private String username;
    private String displayName;
    private String email;
    private Role role;
    private boolean hostConfirmed;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isHostConfirmed() {
        return hostConfirmed;
    }

    public void setHostConfirmed(boolean hostConfirmed) {
        this.hostConfirmed = hostConfirmed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
