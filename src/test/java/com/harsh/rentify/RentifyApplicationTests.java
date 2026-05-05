package com.harsh.rentify;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.harsh.rentify.dto.request.CreateRoomRequest;
import com.harsh.rentify.dto.request.BookingRequest;
import com.harsh.rentify.entity.RoomEntity;
import com.harsh.rentify.entity.UserEntity;
import com.harsh.rentify.repository.RoomRepository;
import com.harsh.rentify.repository.RoomTypeRepository;
import com.harsh.rentify.repository.UserRepository;
import com.harsh.rentify.service.ReservationService;
import com.harsh.rentify.service.RoomService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class RentifyApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void publicPagesRender() throws Exception {
        Long roomId = ensureRoomExists();
        mockMvc.perform(get("/")).andExpect(status().isOk());
        mockMvc.perform(get("/login")).andExpect(status().isOk());
        mockMvc.perform(get("/register")).andExpect(status().isOk());
        mockMvc.perform(get("/rooms")).andExpect(status().isOk());
        mockMvc.perform(get("/rooms/" + roomId)).andExpect(status().isOk());
        mockMvc.perform(get("/profiles/landlord")).andExpect(status().isOk());
    }

    @Test
    void authPagesRenderPasswordToggleControls() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-password-toggle")))
                .andExpect(content().string(containsString("fa fa-eye")))
                .andExpect(content().string(containsString("font-awesome.min.css")));

        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-password-toggle")))
                .andExpect(content().string(containsString("fa fa-eye")));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminDashboardRenders() throws Exception {
        Long roomId = ensureAdminDashboardRoomExists();
        ensureAdminReservationExists(roomId);

        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Welcome,")))
                .andExpect(content().string(containsString("cdn.jsdelivr.net/npm/chart.js")))
                .andExpect(content().string(containsString("adminUserChart")))
                .andExpect(content().string(containsString("Total Users")))
                .andExpect(content().string(containsString("Total Landlords")))
                .andExpect(content().string(containsString("Total Tenants")))
                .andExpect(content().string(containsString("Total Admins")))
                .andExpect(content().string(containsString("Total Properties")))
                .andExpect(content().string(containsString("Total Bookings")))
                .andExpect(content().string(containsString("Total Revenue")))
                .andExpect(content().string(containsString("Admin dashboard")))
                .andExpect(content().string(containsString("Platform user distribution")))
                .andExpect(content().string(containsString("Monthly platform revenue")));

        mockMvc.perform(get("/admin-dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Total Users")));

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"userSearch\"")))
                .andExpect(content().string(containsString("Landlord list")))
                .andExpect(content().string(containsString("Tenant list")))
                .andExpect(content().string(containsString("Mira Kapoor")))
                .andExpect(content().string(containsString("Arjun Shah")))
                .andExpect(content().string(containsString("landlord@rentify.local")))
                .andExpect(content().string(containsString("tenant@rentify.local")));

        mockMvc.perform(get("/admin/properties"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("All properties")))
                .andExpect(content().string(containsString("Admin Dashboard Listing")));

        mockMvc.perform(get("/admin/bookings"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("All bookings")))
                .andExpect(content().string(containsString("Admin Dashboard Listing")))
                .andExpect(content().string(containsString("Arjun Shah")));
    }

    @Test
    @WithMockUser(username = "landlord", roles = "LANDLORD")
    void landlordDashboardRenders() throws Exception {
        mockMvc.perform(get("/landlord/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Welcome,")))
                .andExpect(content().string(containsString("landlordChart")))
                .andExpect(content().string(containsString("Total Properties")))
                .andExpect(content().string(containsString("Total Bookings Received")))
                .andExpect(content().string(containsString("Total Revenue")))
                .andExpect(content().string(containsString("Pending Approvals")));

        mockMvc.perform(get("/landlord/properties"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("My properties")));

        mockMvc.perform(get("/landlord/bookings"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Booking requests")));

        mockMvc.perform(get("/landlord/create-property"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("action=\"/add-property\"")));
    }

    @Test
    @WithMockUser(username = "landlord", roles = "LANDLORD")
    void addPropertyFormUsesSubmitRouteCsrfAndDecimalPriceSteps() throws Exception {
        mockMvc.perform(get("/add-property"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("action=\"/add-property\"")))
                .andExpect(content().string(containsString("name=\"_csrf\"")))
                .andExpect(content().string(containsString("name=\"overnightPrice\"")))
                .andExpect(content().string(containsString("name=\"extraPersonPrice\"")))
                .andExpect(content().string(containsString("step=\"0.01\"")));
    }

    @Test
    @WithMockUser(username = "tenant", roles = "TENANT")
    void tenantDashboardRenders() throws Exception {
        mockMvc.perform(get("/tenant/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Welcome,")))
                .andExpect(content().string(containsString("tenantChart")))
                .andExpect(content().string(containsString("Total Bookings")))
                .andExpect(content().string(containsString("Total Spending")))
                .andExpect(content().string(containsString("Approved Bookings")))
                .andExpect(content().string(containsString("Pending Bookings")))
                .andExpect(content().string(containsString("Cancelled Bookings")));

        mockMvc.perform(get("/tenant/bookings"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("My bookings")));

        mockMvc.perform(get("/tenant/properties"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Browse properties")));

        mockMvc.perform(get("/tenant/payments"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Payments")));
    }

    @Test
    void dashboardRedirectsAnonymousUsersToLogin() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser(username = "tenant", roles = "TENANT")
    void bookingEndpointAcceptsValidRequest() throws Exception {
        Long roomId = ensureRoomExists();
        mockMvc.perform(post("/rooms/" + roomId + "/book")
                        .with(csrf())
                        .param("checkInDate", "2030-04-01")
                        .param("checkOutDate", "2030-04-05")
                        .param("guests", "2"))
                .andExpect(status().is3xxRedirection());
    }

    private Long ensureRoomExists() {
        if (roomRepository.count() > 0) {
            return roomRepository.findAll().get(0).getId();
        }

        CreateRoomRequest request = new CreateRoomRequest();
        request.setTitle("Baseline Test Listing");
        request.setDescription("Listing used by context smoke tests.");
        request.setTypeId(roomTypeRepository.findAll().get(0).getId());
        request.setOvernightPrice(new BigDecimal("2500.00"));
        request.setExtraPersonPrice(new BigDecimal("250.00"));
        request.setMaxPeople(2);
        request.setMinOvernights(1);
        request.setBeds(1);
        request.setBathrooms(1);
        request.setBedrooms(1);
        request.setSquareMeters(40);
        request.setFormattedAddress("10 Test Street, Bengaluru, India");
        request.setLocality("Bengaluru");
        request.setCountry("India");
        request.setPostalCode("560001");
        request.setNeighborhood("Test Block");
        request.setHouseRulesText("Standard rules apply.");

        return roomService.createRoom(
                userRepository.findByUsername("landlord").orElseThrow(),
                request,
                null
        ).getId();
    }

    private Long ensureAdminDashboardRoomExists() {
        return roomRepository.findAll().stream()
                .filter(room -> "Admin Dashboard Listing".equals(room.getTitle()))
                .findFirst()
                .map(RoomEntity::getId)
                .orElseGet(() -> {
                    CreateRoomRequest request = new CreateRoomRequest();
                    request.setTitle("Admin Dashboard Listing");
                    request.setDescription("Listing created for admin dashboard rendering tests.");
                    request.setTypeId(roomTypeRepository.findAll().get(0).getId());
                    request.setOvernightPrice(new BigDecimal("3200.00"));
                    request.setExtraPersonPrice(new BigDecimal("300.00"));
                    request.setMaxPeople(3);
                    request.setMinOvernights(1);
                    request.setBeds(2);
                    request.setBathrooms(1);
                    request.setBedrooms(1);
                    request.setSquareMeters(55);
                    request.setFormattedAddress("88 Admin Street, Pune, India");
                    request.setLocality("Pune");
                    request.setCountry("India");
                    request.setPostalCode("411001");
                    request.setNeighborhood("Koregaon Park");
                    request.setHouseRulesText("Keep the property tidy.");

                    return roomService.createRoom(
                            userRepository.findByUsername("landlord").orElseThrow(),
                            request,
                            null
                    ).getId();
                });
    }

    private void ensureAdminReservationExists(Long roomId) {
        UserEntity tenant = userRepository.findByUsername("tenant").orElseThrow();
        boolean exists = reservationService.getTenantReservations(tenant).stream()
                .anyMatch(reservation -> reservation.getRoomId().equals(roomId));
        if (exists) {
            return;
        }

        BookingRequest request = new BookingRequest();
        request.setCheckInDate(LocalDate.of(2031, 5, 10));
        request.setCheckOutDate(LocalDate.of(2031, 5, 14));
        request.setGuests(2);

        reservationService.createReservation(roomId, request, tenant);
    }
}
