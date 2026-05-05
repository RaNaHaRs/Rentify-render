package com.harsh.rentify.config;

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
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataSeeder implements CommandLineRunner {

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

    @Value("${app.bootstrap.admin-username}")
    private String adminUsername;

    @Value("${app.bootstrap.admin-password}")
    private String adminPassword;

    @Value("${app.bootstrap.landlord-username}")
    private String landlordUsername;

    @Value("${app.bootstrap.landlord-password}")
    private String landlordPassword;

    @Value("${app.bootstrap.tenant-username}")
    private String tenantUsername;

    @Value("${app.bootstrap.tenant-password}")
    private String tenantPassword;

    public DataSeeder(
            UserRepository userRepository,
            RoomTypeRepository roomTypeRepository,
            AmenityRepository amenityRepository,
            RuleRepository ruleRepository,
            TransportRepository transportRepository,
            RoomRepository roomRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.amenityRepository = amenityRepository;
        this.ruleRepository = ruleRepository;
        this.transportRepository = transportRepository;
        this.roomRepository = roomRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedReferenceData();
        removeSeededRooms();
        if (userRepository.count() > 0) {
            return;
        }

        UserEntity admin = createUser(adminUsername, "admin@rentify.local", adminPassword, Role.ADMIN, true, "System", "Admin", "Bengaluru, India");
        UserEntity landlord = createUser(landlordUsername, "landlord@rentify.local", landlordPassword, Role.LANDLORD, true, "Mira", "Kapoor", "Goa, India");
        UserEntity tenant = createUser(tenantUsername, "tenant@rentify.local", tenantPassword, Role.TENANT, true, "Arjun", "Shah", "Pune, India");
        UserEntity pendingLandlord = createUser("pendinghost", "pending@rentify.local", landlordPassword, Role.LANDLORD, false, "Nina", "Rao", "Jaipur, India");

        userRepository.saveAll(List.of(admin, landlord, tenant, pendingLandlord));
    }

    private void seedReferenceData() {
        if (roomTypeRepository.count() == 0) {
            roomTypeRepository.saveAll(List.of(
                    createType("Apartment", "apartment"),
                    createType("Loft", "loft"),
                    createType("Villa", "villa"),
                    createType("Studio", "studio")
            ));
        }
        if (amenityRepository.count() == 0) {
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
            ruleRepository.saveAll(List.of(
                    createRule("No smoking"),
                    createRule("Quiet hours after 10 PM"),
                    createRule("No parties")
            ));
        }
        if (transportRepository.count() == 0) {
            transportRepository.saveAll(List.of(
                    createTransport("Metro within 10 minutes"),
                    createTransport("Bus stop within 5 minutes"),
                    createTransport("Airport within 30 minutes"),
                    createTransport("Downtown within 15 minutes")
            ));
        }
    }

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

    private void removeSeededRooms() {
        List<com.harsh.rentify.entity.RoomEntity> sampleRooms =
                roomRepository.findByHostUsernameAndTitleIn(landlordUsername, SAMPLE_ROOM_TITLES);
        if (!sampleRooms.isEmpty()) {
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
