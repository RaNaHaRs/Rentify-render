package com.harsh.rentify.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for bootstrap (initial data seeding).
 * Centralizes all bootstrap-related configuration in one place.
 *
 * Usage in application.yml:
 * app:
 *   bootstrap:
 *     enabled: true
 *     admin-username: admin
 *     admin-password: ${BOOTSTRAP_ADMIN_PASSWORD}
 *     landlord-username: landlord
 *     landlord-password: ${BOOTSTRAP_LANDLORD_PASSWORD}
 *     tenant-username: tenant
 *     tenant-password: ${BOOTSTRAP_TENANT_PASSWORD}
 */
@Component
@ConfigurationProperties(prefix = "app.bootstrap")
public class BootstrapProperties {

    /**
     * Enable or disable data seeding. Defaults to true in dev profile.
     * Set to false in production to prevent automatic user creation.
     */
    private boolean enabled = true;

    /**
     * Admin user credentials
     */
    private String adminUsername;
    private String adminPassword;

    /**
     * Landlord user credentials
     */
    private String landlordUsername;
    private String landlordPassword;

    /**
     * Tenant user credentials
     */
    private String tenantUsername;
    private String tenantPassword;

    // Getters and Setters
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public String getLandlordUsername() {
        return landlordUsername;
    }

    public void setLandlordUsername(String landlordUsername) {
        this.landlordUsername = landlordUsername;
    }

    public String getLandlordPassword() {
        return landlordPassword;
    }

    public void setLandlordPassword(String landlordPassword) {
        this.landlordPassword = landlordPassword;
    }

    public String getTenantUsername() {
        return tenantUsername;
    }

    public void setTenantUsername(String tenantUsername) {
        this.tenantUsername = tenantUsername;
    }

    public String getTenantPassword() {
        return tenantPassword;
    }

    public void setTenantPassword(String tenantPassword) {
        this.tenantPassword = tenantPassword;
    }

    /**
     * Validates that all required passwords are provided
     * @return true if all passwords are set and non-empty
     */
    public boolean hasAllPasswords() {
        return adminPassword != null && !adminPassword.isBlank()
                && landlordPassword != null && !landlordPassword.isBlank()
                && tenantPassword != null && !tenantPassword.isBlank();
    }
}
