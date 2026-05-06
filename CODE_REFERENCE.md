# Production-Ready Configuration - Complete Code Reference

## 1. BootstrapProperties.java (NEW)
Location: `src/main/java/com/harsh/rentify/config/BootstrapProperties.java`

```java
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

    private boolean enabled = true;
    private String adminUsername;
    private String adminPassword;
    private String landlordUsername;
    private String landlordPassword;
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
```

---

## 2. DataSeeder.java (UPDATED)
Location: `src/main/java/com/harsh/rentify/config/DataSeeder.java`

Key changes:
- Injects `BootstrapProperties` instead of individual `@Value` fields
- Logs all operations with SLF4J
- Validates environment variables before creating users
- Skips user creation if seeding is disabled
- Gracefully handles missing passwords

---

## 3. application.yml (UPDATED)
Location: `src/main/resources/application.yml`

```yaml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
    driver-class-name: ${SPRING_DATASOURCE_DRIVER_CLASS_NAME:com.mysql.cj.jdbc.Driver}
  jpa:
    database-platform: org.hibernate.dialect.MySQL8Dialect
    hibernate:
      ddl-auto: update
    open-in-view: false
  thymeleaf:
    cache: false
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 20MB

server:
  port: ${PORT:8080}
  error:
    whitelabel:
      enabled: false

# Application Configuration
app:
  name: Rentify
  bootstrap:
    # Default: disabled (production-safe)
    # Set APP_BOOTSTRAP_ENABLED=true to enable auto-seeding
    # In dev profile, this is overridden to 'true'
    enabled: ${APP_BOOTSTRAP_ENABLED:false}
    admin-username: ${BOOTSTRAP_ADMIN_USERNAME:admin}
    admin-password: ${BOOTSTRAP_ADMIN_PASSWORD}
    landlord-username: ${BOOTSTRAP_LANDLORD_USERNAME:landlord}
    landlord-password: ${BOOTSTRAP_LANDLORD_PASSWORD}
    tenant-username: ${BOOTSTRAP_TENANT_USERNAME:tenant}
    tenant-password: ${BOOTSTRAP_TENANT_PASSWORD}

# Cloudinary Configuration (Cloud Storage)
cloudinary:
  cloud-name: ${CLOUDINARY_CLOUD_NAME}
  api-key: ${CLOUDINARY_API_KEY}
  api-secret: ${CLOUDINARY_API_SECRET}
```

---

## 4. application-dev.yml (UPDATED)
Location: `src/main/resources/application-dev.yml`

```yaml
# Development Profile - Auto-enables seeding and debug logging

spring:
  datasource:
    url: ${DEV_DB_URL:jdbc:mysql://localhost:3306/rental?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC}
    username: ${DEV_DB_USERNAME:root}
    password: ${DEV_DB_PASSWORD}
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true

# Enable user seeding in development
app:
  bootstrap:
    enabled: true
```

---

## 5. application-prod.yml (NO CHANGES NEEDED)
Location: `src/main/resources/application-prod.yml`

The production profile remains unchanged. Users are NOT auto-created because:
- `app.bootstrap.enabled` defaults to `false` (from application.yml)
- Production environment variables likely won't have passwords set
- Manual user creation is safer in production

---

## 6. .env.dev (NEW - FOR REFERENCE)
Location: `.env.dev` (use as template, never commit)

```env
# Development Environment Variables
# For IntelliJ: Copy these values into Run Configuration → Environment variables

DEV_DB_URL=jdbc:mysql://localhost:3306/rental?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DEV_DB_USERNAME=root
DEV_DB_PASSWORD=YourLocalPassword

BOOTSTRAP_ADMIN_PASSWORD=admin123
BOOTSTRAP_LANDLORD_PASSWORD=landlord123
BOOTSTRAP_TENANT_PASSWORD=tenant123

CLOUDINARY_CLOUD_NAME=your-cloud-name
CLOUDINARY_API_KEY=your-api-key
CLOUDINARY_API_SECRET=your-api-secret
```

---

## 7. .env.prod (NEW - FOR REFERENCE)
Location: `.env.prod` (for documentation only, use platform's secrets manager)

```env
# Production Environment Variables
# IMPORTANT: Do NOT commit real values to git!
# Use Render, AWS Secrets Manager, or your platform's environment management

SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:mysql://your-rds-host:3306/rentify_prod?useSSL=true
SPRING_DATASOURCE_USERNAME=db_user
SPRING_DATASOURCE_PASSWORD=<strong-db-password>

BOOTSTRAP_ADMIN_PASSWORD=<strong-admin-password>
BOOTSTRAP_LANDLORD_PASSWORD=<strong-landlord-password>
BOOTSTRAP_TENANT_PASSWORD=<strong-tenant-password>

CLOUDINARY_CLOUD_NAME=your-cloud
CLOUDINARY_API_KEY=your-key
CLOUDINARY_API_SECRET=your-secret
```

---

## How It Works

### Development Flow
1. Application starts with `SPRING_PROFILES_ACTIVE=dev`
2. Loads `application.yml` + `application-dev.yml`
3. `app.bootstrap.enabled = true` (from dev profile)
4. DataSeeder runs and:
   - Loads BootstrapProperties with values from environment variables
   - Validates all passwords are provided
   - Creates default users (admin, landlord, tenant, pendinghost)

### Production Flow
1. Application starts with `SPRING_PROFILES_ACTIVE=prod`
2. Loads `application.yml` + `application-prod.yml`
3. `app.bootstrap.enabled = false` (default from application.yml)
4. DataSeeder runs and:
   - Logs "User seeding is disabled"
   - Only creates reference data (safe)
   - Skips user creation

---

## Environment Variable Priority

Spring Boot processes variables in this order:

1. **OS Environment Variables** (highest priority)
2. **System Properties** (java -Dkey=value)
3. **application.yml** (base config)
4. **application-{profile}.yml** (profile-specific overrides)
5. **Default values** in `${VAR:default}` syntax

Example resolution:
```
${BOOTSTRAP_ADMIN_PASSWORD}
  ↓
Check OS environment → If not found
  ↓
Check application.yml for default → If not found
  ↓
Value is null/empty → DataSeeder logs warning
```

---

## Security Best Practices Applied

✅ **No Hardcoded Secrets** - All credentials externalized
✅ **Production-Safe Defaults** - Seeding disabled by default
✅ **Validation** - Checks for required variables before use
✅ **Graceful Degradation** - Won't crash if variables missing
✅ **Logging** - Clear audit trail of what's happening
✅ **Profile Separation** - Dev behavior differs from prod
✅ **Idempotent Operations** - Safe to run multiple times
✅ **Clean Code** - Uses Spring Boot 3.x best practices

---

## Testing Configuration Changes

```bash
# Test with dev profile
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Test with prod profile
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"

# Check if seeding runs
# Should see logs: "Seeding default users..." or "User seeding is disabled"
```

---

## Troubleshooting Checklist

- [ ] Environment variables are set correctly in IDE
- [ ] All three password variables are provided (if enabling seeding)
- [ ] Database is accessible with provided credentials
- [ ] Spring profile is set to `dev` for development
- [ ] Check logs for "DataSeeder" messages to understand what happened
- [ ] If users don't exist, verify `app.bootstrap.enabled = true` in logs
