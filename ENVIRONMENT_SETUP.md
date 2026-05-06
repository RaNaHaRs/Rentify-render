# Rentify - Environment Variables & Data Seeding Setup

## Overview

This document explains how to configure Rentify with environment variables for both development and production environments.

## Configuration Architecture

The application uses a three-tier configuration system:

1. **application.yml** - Base configuration (applies to all profiles)
2. **application-dev.yml** - Development overrides
3. **application-prod.yml** - Production overrides
4. **.env files** - Local environment variables (NOT committed to git)

## Development Setup

### Step 1: Set Environment Variables Locally

For **local development**, create a `.env.local` file in the project root (this file is in `.gitignore` and won't be committed):

```env
# .env.local

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

### Step 2: Load Environment Variables in Your IDE

#### For IntelliJ IDEA:
1. Open Run → Edit Configurations
2. Select your Spring Boot run configuration
3. Under "Environment variables", paste:
```
DEV_DB_URL=jdbc:mysql://localhost:3306/rental?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC;DEV_DB_USERNAME=root;DEV_DB_PASSWORD=YourPassword;BOOTSTRAP_ADMIN_PASSWORD=admin123;BOOTSTRAP_LANDLORD_PASSWORD=landlord123;BOOTSTRAP_TENANT_PASSWORD=tenant123;CLOUDINARY_CLOUD_NAME=your-cloud;CLOUDINARY_API_KEY=key;CLOUDINARY_API_SECRET=secret
```

#### For VS Code with Spring Boot Extension:
1. Install the "Spring Boot Extension Pack"
2. Create `.vscode/launch.json`:
```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "name": "Spring Boot App",
      "type": "java",
      "name": "Spring Boot App",
      "request": "launch",
      "env": {
        "DEV_DB_URL": "jdbc:mysql://localhost:3306/rental?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
        "DEV_DB_USERNAME": "root",
        "DEV_DB_PASSWORD": "YourPassword",
        "BOOTSTRAP_ADMIN_PASSWORD": "admin123",
        "BOOTSTRAP_LANDLORD_PASSWORD": "landlord123",
        "BOOTSTRAP_TENANT_PASSWORD": "tenant123",
        "CLOUDINARY_CLOUD_NAME": "your-cloud",
        "CLOUDINARY_API_KEY": "key",
        "CLOUDINARY_API_SECRET": "secret"
      }
    }
  ]
}
```

### Step 3: Start the Application

When you run the application in development mode:

1. The profile `dev` is activated (via `SPRING_PROFILES_ACTIVE=dev` or default)
2. `application.yml` + `application-dev.yml` are loaded
3. DataSeeder automatically:
   - Loads the BootstrapProperties from configuration
   - Seeds reference data (room types, amenities, rules, transport)
   - Creates default users: **admin**, **landlord**, **tenant**, **pendinghost**

### Step 4: Access the Application

- **URL**: http://localhost:8080
- **Admin Login**: 
  - Username: `admin`
  - Password: `admin123` (from BOOTSTRAP_ADMIN_PASSWORD)
- **Landlord Login**:
  - Username: `landlord`
  - Password: `landlord123` (from BOOTSTRAP_LANDLORD_PASSWORD)
- **Tenant Login**:
  - Username: `tenant`
  - Password: `tenant123` (from BOOTSTRAP_TENANT_PASSWORD)

## Production Setup (Render/AWS/Heroku)

### Step 1: Set Environment Variables on Your Platform

On **Render.com**, go to **Environment**:

```
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:mysql://your-rds-endpoint:3306/rentify_prod?useSSL=true
SPRING_DATASOURCE_USERNAME=admin_user
SPRING_DATASOURCE_PASSWORD=<strong-password>
BOOTSTRAP_ADMIN_PASSWORD=<strong-admin-password>
BOOTSTRAP_LANDLORD_PASSWORD=<strong-landlord-password>
BOOTSTRAP_TENANT_PASSWORD=<strong-tenant-password>
CLOUDINARY_CLOUD_NAME=<your-cloud>
CLOUDINARY_API_KEY=<your-key>
CLOUDINARY_API_SECRET=<your-secret>
```

### Step 2: Understand DataSeeder Behavior in Production

In production (`application-prod.yml`):
- `app.bootstrap.enabled = false` (default from `application.yml`)
- DataSeeder will:
  1. Always seed reference data (safe, idempotent)
  2. Skip user creation (logging a warning)
  3. Not create users even if environment variables are present

This is **intentional** for security:
- Prevents accidental user creation in production
- Avoids default passwords lingering in production

### Step 3: Creating Production Users

#### Option A: Enable via Environment Variable (if you want auto-seeding)
Set on Render:
```
APP_BOOTSTRAP_ENABLED=true
```

Then DataSeeder will create users **only on first run** if the database is empty.

#### Option B: Create Users Manually (Recommended)
1. Access the application admin panel
2. Create users manually with strong passwords
3. Assign roles appropriately

#### Option C: Use a Database Script
After deployment, run a SQL script:
```sql
-- Never commit this to git!
INSERT INTO users (username, email, password, role, enabled, host_confirmed) VALUES
('admin', 'admin@rentify.com', '$2a$10$...', 'ADMIN', 1, 1);
```

## Configuration Reference

### application.yml (Base Configuration)

```yaml
app:
  bootstrap:
    enabled: ${APP_BOOTSTRAP_ENABLED:false}  # Default: disabled (production-safe)
    admin-username: ${BOOTSTRAP_ADMIN_USERNAME:admin}
    admin-password: ${BOOTSTRAP_ADMIN_PASSWORD}  # REQUIRED if enabled=true
    landlord-username: ${BOOTSTRAP_LANDLORD_USERNAME:landlord}
    landlord-password: ${BOOTSTRAP_LANDLORD_PASSWORD}  # REQUIRED if enabled=true
    tenant-username: ${BOOTSTRAP_TENANT_USERNAME:tenant}
    tenant-password: ${BOOTSTRAP_TENANT_PASSWORD}  # REQUIRED if enabled=true
```

### application-dev.yml (Development Overrides)

```yaml
app:
  bootstrap:
    enabled: true  # Auto-seed in development
```

### application-prod.yml (Production Overrides)

```yaml
spring:
  jpa:
    show-sql: false
    hibernate:
      ddl-auto: validate
  thymeleaf:
    cache: true

logging:
  level:
    root: WARN
```

## DataSeeder Behavior

### When Seeding Runs:

1. **Reference Data** (always safe to run):
   - Room types (Apartment, Loft, Villa, Studio)
   - Amenities (Wi-Fi, Parking, Kitchen, etc.)
   - House rules
   - Transport options
   - Only created if they don't exist

2. **User Data** (controlled by flag and variables):
   ```
   if (bootstrap.enabled && password_variables_present && no_users_in_db) {
       Create admin, landlord, tenant, pendinghost users
   }
   ```

### Safety Features:

✅ Won't duplicate data (idempotent)
✅ Won't create users if they already exist
✅ Won't crash if passwords are missing
✅ Logs all actions for debugging
✅ Profile-aware (dev vs prod)
✅ Removable by setting `enabled: false`

## Troubleshooting

### Problem: "Cannot seed users: Missing required password environment variables"

**Solution**: Ensure all three password variables are set:
- `BOOTSTRAP_ADMIN_PASSWORD`
- `BOOTSTRAP_LANDLORD_PASSWORD`
- `BOOTSTRAP_TENANT_PASSWORD`

### Problem: Users already exist, but I want to re-seed

**Solution**: Delete all users from the database:
```sql
DELETE FROM users;
```

Then restart the application.

### Problem: DataSeeder not running in production

**This is intentional**. DataSeeder only runs if:
1. `app.bootstrap.enabled = true`
2. All password environment variables are provided
3. No users exist in the database

This prevents accidental data creation in production.

## Best Practices

### ✅ Development
- Use simple passwords (e.g., `admin123`)
- Set `APP_BOOTSTRAP_ENABLED=true` or use `dev` profile
- Never commit `.env.local` or IDE run configurations with real passwords

### ✅ Production
- Use **strong, randomly generated** passwords (20+ characters)
- Store passwords in platform's **secrets manager** (Render, AWS Secrets Manager, etc.)
- Set `APP_BOOTSTRAP_ENABLED=false` (default)
- Never commit `.env.prod` to version control
- Manually create production users via admin panel
- Regularly rotate passwords

### ✅ Version Control
```
# .gitignore
.env*
.env.local
.env.prod
.idea/
```

## Summary

| Aspect | Development | Production |
|--------|-------------|-----------|
| Profile | `dev` | `prod` |
| Bootstrap Enabled | ✅ Yes | ❌ No |
| Auto-Seed Users | ✅ Yes | ❌ No |
| SQL Logging | ✅ Enabled | ❌ Disabled |
| Thymeleaf Cache | ❌ No | ✅ Yes |
| Password Handling | Simple values | Strong secrets |
| Password Storage | .env.local | Platform secrets |

## Links & Resources

- [Spring Boot Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [Spring Boot Profiles](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.profiles)
- [ConfigurationProperties](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config.typesafe-configuration-properties)
- [Render.com Environment Variables](https://render.com/docs/environment-variables)
- [AWS Secrets Manager](https://aws.amazon.com/secrets-manager/)
