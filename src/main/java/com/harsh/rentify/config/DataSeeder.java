package com.harsh.rentify.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.harsh.rentify.entity.AmenityEntity;
import com.harsh.rentify.entity.LocationEntity;
import com.harsh.rentify.entity.ProfileEntity;
import com.harsh.rentify.entity.Role;
import com.harsh.rentify.entity.RoomTypeEntity;
import com.harsh.rentify.entity.RuleEntity;
import com.harsh.rentify.entity.TransportEntity;
import com.harsh.rentify.entity.UserEntity;
import com.harsh.rentify.repository.AmenityRepository;
import com.harsh.rentify.repository.RoomRepository;
import com.harsh.rentify.repository.RoomTypeRepository;
import com.harsh.rentify.repository.RuleRepository;
import com.harsh.rentify.repository.TransportRepository;
import com.harsh.rentify.repository.UserRepository;

/**
 * DataSeeder initializes the database with reference data and default users.
 * 
 * Features:
 * - Controlled via app.bootstrap.enabled flag (defaults to true in dev profile)
 * - Creates users only if they don't already exist
 * - Gracefully handles missing environment variables
 * - Profile-aware (runs in dev profile by default)
 * 
 * To disable seeding in development:
 *   Set app.bootstrap.enabled=false in application-dev.yml
 * 
 * Required environment variables (only if seeding is enabled):
 *   BOOTSTRAP_ADMIN_PASSWORD
 *   BOOTSTRAP_LANDLORD_PASSWORD
 *   BOOTSTRAP_TENANT_PASSWORD
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    private static final List<String> SAMPLE_ROOM_TITLES = List.of(
            "Sunlit River Loft",
            "Palm Court Villa",
            "Minimal City Studio"
    );

    private final UserRepository userRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final AmenityRepository amenityRepository;
    private final RuleRepository ruleRepository;
    private final TransportRepository transportRepository;
    private final RoomRepository roomRepository;
    private final PasswordEncoder passwordEncoder;
    private final BootstrapProperties bootstrapProperties;

    public DataSeeder(
            UserRepository userRepository,
            RoomTypeRepository roomTypeRepository,
            AmenityRepository amenityRepository,
            RuleRepository ruleRepository,
            TransportRepository transportRepository,
            RoomRepository roomRepository,
            PasswordEncoder passwordEncoder,
            BootstrapProperties bootstrapProperties
    ) {
        this.userRepository = userRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.amenityRepository = amenityRepository;
        this.ruleRepository = ruleRepository;
        this.transportRepository = transportRepository;
        this.roomRepository = roomRepository;
        this.passwordEncoder = passwordEncoder;
        this.bootstrapProperties = bootstrapProperties;
    }

    @Override
    @Transactional
    public void run(String... args) {
        logger.info("Starting DataSeeder initialization...");

        // Seed reference data (always safe to run)
        seedReferenceData();
        removeSeededRooms();

        // Seed user data (controlled by flag and environment variables)
        seedUsers();

        logger.info("DataSeeder initialization completed.");
    }

    /**
     * Seeds reference data (room types, amenities, rules, transport).
     * Always runs regardless of bootstrap enabled flag.
     */
    private void seedReferenceData() {
        if (roomTypeRepository.count() == 0) {
            logger.info("Seeding room types...");
            roomTypeRepository.saveAll(List.of(
                    createType("Apartment", "apartment"),
                    createType("Loft", "loft"),
                    createType("Villa", "villa"),
                    createType("Studio", "studio")
            ));
        }
        if (amenityRepository.count() == 0) {
            logger.info("Seeding amenities...");
            amenityRepository.saveAll(List.of(
                    createAmenity("Wi-Fi", "bi-wifi"),
                    createAmenity("Parking", "bi-p-circle"),
                    createAmenity("Kitchen", "bi-cup-hot"),
                    createAmenity("Washer", "bi-droplet"),
                    createAmenity("Air conditioning", "bi-snow"),
                    createAmenity("Pool", "bi-water")
            ));
        }
        if (ruleRepository.count() == 0) {
            logger.info("Seeding rules...");
            ruleRepository.saveAll(List.of(
                    createRule("No smoking"),
                    createRule("Quiet hours after 10 PM"),
                    createRule("No parties")
            ));
        }
        if (transportRepository.count() == 0) {
            logger.info("Seeding transport options...");
            transportRepository.saveAll(List.of(
                    createTransport("Metro within 10 minutes"),
                    createTransport("Bus stop within 5 minutes"),
                    createTransport("Airport within 30 minutes"),
                    createTransport("Downtown within 15 minutes")
            ));
        }
    }

    /**
     * Seeds default users (admin, landlord, tenant).
     * Only runs if:
     * - app.bootstrap.enabled = true
     * - All required password environment variables are provided
     * - Users don't already exist in the database
     */
    private void seedUsers() {
        // Check if seeding is disabled
        if (!bootstrapProperties.isEnabled()) {
            logger.info("User seeding is disabled (app.bootstrap.enabled=false). Skipping user creation.");
            return;
        }

        // Check if all required passwords are provided
        if (!bootstrapProperties.hasAllPasswords()) {
            logger.warn("Cannot seed users: Missing required password environment variables.");
            logger.warn("Required: BOOTSTRAP_ADMIN_PASSWORD, BOOTSTRAP_LANDLORD_PASSWORD, BOOTSTRAP_TENANT_PASSWORD");
            logger.warn("Skipping user creation. Users can be created manually or via admin panel.");
            return;
        }

        // Check if users already exist
        if (userRepository.count() > 0) {
            logger.info("Users already exist in database. Skipping user creation.");
            return;
        }

        logger.info("Seeding default users...");

        // Create default users
        UserEntity admin = createUser(
                bootstrapProperties.getAdminUsername(),
                "admin@rentify.local",
                bootstrapProperties.getAdminPassword(),
                Role.ADMIN,
                true,
                "System",
                "Admin",
                "Bengaluru, India"
        );

        UserEntity landlord = createUser(
                bootstrapProperties.getLandlordUsername(),
                "landlord@rentify.local",
                bootstrapProperties.getLandlordPassword(),
                Role.LANDLORD,
                true,
                "Mira",
                "Kapoor",
                "Goa, India"
        );

        UserEntity tenant = createUser(
                bootstrapProperties.getTenantUsername(),
                "tenant@rentify.local",
                bootstrapProperties.getTenantPassword(),
                Role.TENANT,
                true,
                "Arjun",
                "Shah",
                "Pune, India"
        );

        UserEntity pendingLandlord = createUser(
                "pendinghost",
                "pending@rentify.local",
                bootstrapProperties.getLandlordPassword(),
                Role.LANDLORD,
                false,
                "Nina",
                "Rao",
                "Jaipur, India"
        );

        userRepository.saveAll(List.of(admin, landlord, tenant, pendingLandlord));
        logger.info("Default users created successfully.");
    }

    /**
     * Creates a UserEntity with profile and location information.
     *
     * @param username the username
     * @param email the email address
     * @param password the plain text password (will be encoded)
     * @param role the user role (ADMIN, LANDLORD, TENANT)
     * @param hostConfirmed whether the host is confirmed
     * @param firstName the first name
     * @param lastName the last name
     * @param city the city/address
     * @return a configured UserEntity
     */
    private UserEntity createUser(
            String username,
            String email,
            String password,
            Role role,
            boolean hostConfirmed,
            String firstName,
            String lastName,
            String city
    ) {
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setEnabled(true);
        user.setHostConfirmed(hostConfirmed);

        ProfileEntity profile = new ProfileEntity();
        profile.setFirstName(firstName);
        profile.setLastName(lastName);
        profile.setAbout("Sample " + role.name().toLowerCase() + " account for Rentify.");

        LocationEntity location = new LocationEntity();
        location.setFormattedAddress(city);
        location.setLocality(city.split(",")[0].trim());
        location.setCountry("India");
        profile.setLocation(location);

        user.setProfile(profile);
        return user;
    }

    /**
     * Removes sample rooms created during seeding.
     * Useful for resetting demo data during development.
     */
    private void removeSeededRooms() {
        List<com.harsh.rentify.entity.RoomEntity> sampleRooms =
                roomRepository.findByHostUsernameAndTitleIn(
                        bootstrapProperties.getLandlordUsername(),
                        SAMPLE_ROOM_TITLES
                );
        if (!sampleRooms.isEmpty()) {
            logger.info("Removing sample rooms...");
            roomRepository.deleteAll(sampleRooms);
        }
    }

    private RoomTypeEntity createType(String name, String slug) {
        RoomTypeEntity entity = new RoomTypeEntity();
        entity.setName(name);
        entity.setSlug(slug);
        return entity;
    }

    private AmenityEntity createAmenity(String name, String icon) {
        AmenityEntity entity = new AmenityEntity();
        entity.setName(name);
        entity.setIcon(icon);
        return entity;
    }

    private RuleEntity createRule(String name) {
        RuleEntity entity = new RuleEntity();
        entity.setName(name);
        return entity;
    }

    private TransportEntity createTransport(String name) {
        TransportEntity entity = new TransportEntity();
        entity.setName(name);
        return entity;
    }
}
