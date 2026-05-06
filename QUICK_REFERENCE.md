# Quick Reference: Environment Variables Setup

## TL;DR - Development

### 1. Set Environment Variables (IntelliJ IDEA)
Go to **Run → Edit Configurations** → Environment variables:

```
BOOTSTRAP_ADMIN_PASSWORD=admin123;
BOOTSTRAP_LANDLORD_PASSWORD=landlord123;
BOOTSTRAP_TENANT_PASSWORD=tenant123;
DEV_DB_URL=jdbc:mysql://localhost:3306/rental?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC;
DEV_DB_USERNAME=root;
DEV_DB_PASSWORD=YourPassword;
CLOUDINARY_CLOUD_NAME=your-cloud-name;
CLOUDINARY_API_KEY=your-key;
CLOUDINARY_API_SECRET=your-secret
```

### 2. Start the App
- Run the Spring Boot application
- DataSeeder automatically creates users

### 3. Login Credentials
```
Admin:    admin / admin123
Landlord: landlord / landlord123
Tenant:   tenant / tenant123
```

---

## TL;DR - Production (Render.com)

### 1. Set Environment Variables on Render
Go to **Settings → Environment** and add:

```
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:mysql://your-db-host:3306/rentify_prod?useSSL=true
SPRING_DATASOURCE_USERNAME=db_user
SPRING_DATASOURCE_PASSWORD=strong-password
CLOUDINARY_CLOUD_NAME=your-cloud
CLOUDINARY_API_KEY=your-key
CLOUDINARY_API_SECRET=your-secret
```

### 2. Auto-Seeding (Optional)
If you want DataSeeder to create users automatically on first run:

```
APP_BOOTSTRAP_ENABLED=true
BOOTSTRAP_ADMIN_PASSWORD=strong-password
BOOTSTRAP_LANDLORD_PASSWORD=strong-password
BOOTSTRAP_TENANT_PASSWORD=strong-password
```

### 3. Manual User Creation (Recommended)
- Leave `APP_BOOTSTRAP_ENABLED=false` (default)
- Create users manually via admin panel after deployment

---

## Files Changed

### New Files Created:
- `BootstrapProperties.java` - Configuration properties class
- `ENVIRONMENT_SETUP.md` - Complete setup guide
- `.env.dev` - Development environment template
- `.env.prod` - Production environment template

### Modified Files:
- `DataSeeder.java` - Added proper logging, validation, and profile-aware behavior
- `application.yml` - Added `enabled` flag to bootstrap configuration
- `application-dev.yml` - Added `enabled: true` for development

---

## Key Improvements

✅ **No Hardcoded Passwords** - All credentials use environment variables
✅ **Production-Safe** - Seeding disabled by default in production
✅ **Graceful Handling** - Won't crash if environment variables are missing
✅ **Logging** - Clear logs about what's happening during seeding
✅ **Idempotent** - Can run multiple times without duplicating data
✅ **Profile-Aware** - Behavior differs between dev and prod
✅ **Best Practices** - Uses Spring Boot 3.x @ConfigurationProperties

---

## Environment Variables Reference

### Bootstrap Configuration
| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `APP_BOOTSTRAP_ENABLED` | No | `false` | Enable/disable user seeding |
| `BOOTSTRAP_ADMIN_PASSWORD` | If enabled | None | Admin user password |
| `BOOTSTRAP_LANDLORD_PASSWORD` | If enabled | None | Landlord user password |
| `BOOTSTRAP_TENANT_PASSWORD` | If enabled | None | Tenant user password |
| `BOOTSTRAP_ADMIN_USERNAME` | No | `admin` | Admin username |
| `BOOTSTRAP_LANDLORD_USERNAME` | No | `landlord` | Landlord username |
| `BOOTSTRAP_TENANT_USERNAME` | No | `tenant` | Tenant username |

### Database Configuration
| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `SPRING_DATASOURCE_URL` | Yes | None | Database JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | Yes | None | Database username |
| `SPRING_DATASOURCE_PASSWORD` | Yes | None | Database password |
| `DEV_DB_URL` | No | localhost MySQL | Development DB URL |
| `DEV_DB_USERNAME` | No | `root` | Development DB user |
| `DEV_DB_PASSWORD` | No | None | Development DB password |

### Cloudinary (Cloud Storage)
| Variable | Required | Description |
|----------|----------|-------------|
| `CLOUDINARY_CLOUD_NAME` | Yes | Your Cloudinary cloud name |
| `CLOUDINARY_API_KEY` | Yes | Your Cloudinary API key |
| `CLOUDINARY_API_SECRET` | Yes | Your Cloudinary API secret |

---

## Behavior Matrix

### Development (dev profile)
```
app.bootstrap.enabled = true
Result: Users auto-created on first run
```

### Production (prod profile)
```
app.bootstrap.enabled = false (default)
Result: Users NOT auto-created (safe default)
```

### Manual Override
```
APP_BOOTSTRAP_ENABLED=true (set in environment)
Result: Users auto-created if env variables provided
```

---

## Logging Output

When DataSeeder runs, you'll see logs like:

```
Starting DataSeeder initialization...
Seeding room types...
Seeding amenities...
Seeding rules...
Seeding transport options...
User seeding is disabled (app.bootstrap.enabled=false). Skipping user creation.
```

Or:

```
Starting DataSeeder initialization...
Seeding room types...
Seeding amenities...
Seeding rules...
Seeding transport options...
Seeding default users...
Default users created successfully.
```

Or if variables are missing:

```
Cannot seed users: Missing required password environment variables.
Required: BOOTSTRAP_ADMIN_PASSWORD, BOOTSTRAP_LANDLORD_PASSWORD, BOOTSTRAP_TENANT_PASSWORD
Skipping user creation. Users can be created manually or via admin panel.
```
