package com.harsh.rentify;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.harsh.rentify.dto.request.CreateRoomRequest;
import com.harsh.rentify.entity.PaymentEntity;
import com.harsh.rentify.entity.PropertyImageEntity;
import com.harsh.rentify.entity.ReservationEntity;
import com.harsh.rentify.entity.ReservationStatus;
import com.harsh.rentify.entity.ReviewEntity;
import com.harsh.rentify.entity.RoomEntity;
import com.harsh.rentify.entity.UserEntity;
import com.harsh.rentify.repository.AmenityRepository;
import com.harsh.rentify.repository.PaymentRepository;
import com.harsh.rentify.repository.ReservationRepository;
import com.harsh.rentify.repository.ReviewRepository;
import com.harsh.rentify.repository.RoomRepository;
import com.harsh.rentify.repository.RoomTypeRepository;
import com.harsh.rentify.repository.RuleRepository;
import com.harsh.rentify.repository.TransportRepository;
import com.harsh.rentify.repository.UserRepository;
import com.harsh.rentify.service.RoomService;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RoomListingFlowIntegrationTest {

    private static final Path PROPERTY_UPLOAD_DIR = Path.of("target", "test-uploads", "properties");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Autowired
    private AmenityRepository amenityRepository;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private TransportRepository transportRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void landlordCanCreateListingWithMultipleImagesAndTenantCanSeeGallery() throws Exception {
        long initialRoomCount = roomRepository.count();

        byte[] firstImageBytes = "test-image-content-one".getBytes(StandardCharsets.UTF_8);
        byte[] secondImageBytes = "test-image-content-two".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile firstImage = new MockMultipartFile("images", "listing-one.png", "image/png", firstImageBytes);
        MockMultipartFile secondImage = new MockMultipartFile("images", "listing-two.jpg", "image/jpeg", secondImageBytes);
        String title = "Integration Listing";

        mockMvc.perform(multipart("/add-property")
                        .file(firstImage)
                        .file(secondImage)
                        .param("title", title)
                        .param("description", "Fresh listing created through multipart form submission.")
                        .param("typeId", roomTypeRepository.findAll().get(0).getId().toString())
                        .param("overnightPrice", "5500.00")
                        .param("extraPersonPrice", "500.00")
                        .param("maxPeople", "4")
                        .param("minOvernights", "2")
                        .param("beds", "2")
                        .param("bathrooms", "1")
                        .param("bedrooms", "2")
                        .param("squareMeters", "90")
                        .param("formattedAddress", "44 Test Lane, Pune, India")
                        .param("locality", "Pune")
                        .param("country", "India")
                        .param("postalCode", "411001")
                        .param("neighborhood", "Koregaon Park")
                        .param("houseRulesText", "Keep the place clean.")
                        .param("amenityIds", amenityRepository.findAll().get(0).getId().toString())
                        .param("ruleIds", ruleRepository.findAll().get(0).getId().toString())
                        .param("transportIds", transportRepository.findAll().get(0).getId().toString())
                        .with(user("landlord").roles("LANDLORD"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("successMessage", "Listing added successfully!"))
                .andExpect(redirectedUrl("/landlord/properties"));

        assertThat(roomRepository.count()).isEqualTo(initialRoomCount + 1);

        RoomEntity savedRoom = roomRepository.findByHostOrderByCreatedAtDesc(
                        userRepository.findByUsername("landlord").orElseThrow())
                .stream()
                .filter(room -> title.equals(room.getTitle()))
                .findFirst()
                .orElseThrow();

        assertThat(savedRoom.getTitle()).isEqualTo(title);
        assertThat(savedRoom.getImage()).isNull();
        assertThat(savedRoom.getImageContentType()).isNull();
        assertThat(savedRoom.getImages()).hasSize(2);
        assertThat(savedRoom.getPrimaryImageUrl()).isEqualTo(savedRoom.getImages().get(0).getImageUrl());
        assertThat(savedRoom.getImages())
                .extracting(PropertyImageEntity::getImageUrl)
                .allSatisfy(imageUrl -> assertThat(imageUrl).startsWith("/uploads/properties/"));

        String firstImageUrl = savedRoom.getImages().get(0).getImageUrl();
        String secondImageUrl = savedRoom.getImages().get(1).getImageUrl();
        assertThat(PROPERTY_UPLOAD_DIR.resolve(firstImageUrl.substring(firstImageUrl.lastIndexOf('/') + 1))).exists();
        assertThat(PROPERTY_UPLOAD_DIR.resolve(secondImageUrl.substring(secondImageUrl.lastIndexOf('/') + 1))).exists();

        mockMvc.perform(get("/landlord/properties")
                        .flashAttr("successMessage", "Listing added successfully!")
                        .with(user("landlord").roles("LANDLORD")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(title)))
                .andExpect(content().string(containsString("Listing added successfully!")));

        mockMvc.perform(get("/tenant/properties")
                        .with(user("tenant").roles("TENANT")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(title)))
                .andExpect(content().string(containsString(firstImageUrl)));

        mockMvc.perform(get("/rooms/" + savedRoom.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(firstImageUrl)))
                .andExpect(content().string(containsString(secondImageUrl)));

        mockMvc.perform(get(firstImageUrl))
                .andExpect(status().isOk())
                .andExpect(content().bytes(firstImageBytes));

        mockMvc.perform(get("/rooms/" + savedRoom.getId() + "/image"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", firstImageUrl));
    }

    @Test
    void landlordCanDeleteOwnListingAndCascadeRelatedData() throws Exception {
        String title = "Delete Flow Listing";
        RoomEntity savedRoom = createListing(title, "delete-image-content".getBytes(StandardCharsets.UTF_8));
        UserEntity tenant = userRepository.findByUsername("tenant").orElseThrow();

        ReservationEntity reservation = new ReservationEntity();
        reservation.setRoom(savedRoom);
        reservation.setTenant(tenant);
        reservation.setCheckInDate(LocalDate.of(2033, 1, 10));
        reservation.setCheckOutDate(LocalDate.of(2033, 1, 13));
        reservation.setGuests(2);
        reservation.setTotalCost(new BigDecimal("11100.00"));
        reservation.setStatus(ReservationStatus.APPROVED);
        reservation.setPaid(true);
        reservation = reservationRepository.save(reservation);

        ReviewEntity review = new ReviewEntity();
        review.setRoom(savedRoom);
        review.setAuthor(tenant);
        review.setRating(5);
        review.setComment("Excellent stay.");
        review = reviewRepository.save(review);

        PaymentEntity payment = new PaymentEntity();
        payment.setBooking(reservation);
        payment.setAmount(reservation.getTotalCost());
        payment = paymentRepository.save(payment);

        Long roomId = savedRoom.getId();
        Long reservationId = reservation.getId();
        Long reviewId = review.getId();
        Long paymentId = payment.getId();
        String imageUrl = savedRoom.getImages().get(0).getImageUrl();
        Path storedImage = toStoredImagePath(imageUrl);
        assertThat(storedImage).exists();
        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get("/landlord/properties")
                        .with(user("landlord").roles("LANDLORD")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/properties/delete/" + roomId)))
                .andExpect(content().string(containsString("Are you sure you want to delete this property?")));

        mockMvc.perform(post("/properties/delete/" + roomId)
                        .with(user("landlord").roles("LANDLORD"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("successMessage", "Property deleted successfully."))
                .andExpect(redirectedUrl("/landlord/properties"));
        entityManager.clear();

        assertThat(roomRepository.findById(roomId)).isEmpty();
        assertThat(reservationRepository.findById(reservationId)).isEmpty();
        assertThat(reviewRepository.findById(reviewId)).isEmpty();
        assertThat(paymentRepository.findById(paymentId)).isEmpty();
        assertThat(storedImage).doesNotExist();

        mockMvc.perform(get("/landlord/properties")
                        .with(user("landlord").roles("LANDLORD")))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString(title))));
    }

    @Test
    void landlordCannotDeleteAnotherUsersListing() throws Exception {
        String title = "Protected Delete Listing";
        RoomEntity savedRoom = createListing(title, "protected-image-content".getBytes(StandardCharsets.UTF_8));
        Long roomId = savedRoom.getId();
        String imageUrl = savedRoom.getImages().get(0).getImageUrl();
        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(post("/properties/delete/" + roomId)
                        .with(user("pendinghost").roles("LANDLORD"))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("You can delete only your own properties.")));

        entityManager.clear();
        assertThat(roomRepository.findById(roomId)).isPresent();
        assertThat(toStoredImagePath(imageUrl)).exists();
    }

    @Test
    void adminCanDeleteAnyListingAndCascadeRelatedData() throws Exception {
        String title = "Admin Delete Listing";
        RoomEntity savedRoom = createListing(title, "admin-delete-image-content".getBytes(StandardCharsets.UTF_8));
        UserEntity tenant = userRepository.findByUsername("tenant").orElseThrow();

        ReservationEntity reservation = new ReservationEntity();
        reservation.setRoom(savedRoom);
        reservation.setTenant(tenant);
        reservation.setCheckInDate(LocalDate.of(2033, 2, 10));
        reservation.setCheckOutDate(LocalDate.of(2033, 2, 13));
        reservation.setGuests(2);
        reservation.setTotalCost(new BigDecimal("9900.00"));
        reservation.setStatus(ReservationStatus.APPROVED);
        reservation.setPaid(true);
        reservation = reservationRepository.save(reservation);

        ReviewEntity review = new ReviewEntity();
        review.setRoom(savedRoom);
        review.setAuthor(tenant);
        review.setRating(4);
        review.setComment("Managed by admin deletion test.");
        review = reviewRepository.save(review);

        PaymentEntity payment = new PaymentEntity();
        payment.setBooking(reservation);
        payment.setAmount(reservation.getTotalCost());
        payment = paymentRepository.save(payment);

        Long roomId = savedRoom.getId();
        Long reservationId = reservation.getId();
        Long reviewId = review.getId();
        Long paymentId = payment.getId();
        String imageUrl = savedRoom.getImages().get(0).getImageUrl();
        Path storedImage = toStoredImagePath(imageUrl);
        assertThat(storedImage).exists();
        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get("/admin/properties")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/admin/properties/delete/" + roomId)))
                .andExpect(content().string(containsString("Delete this property?")))
                .andExpect(content().string(containsString(title)));

        mockMvc.perform(post("/admin/properties/delete/" + roomId)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("successMessage", "Property deleted by admin"))
                .andExpect(redirectedUrl("/admin/properties"));
        entityManager.clear();

        assertThat(roomRepository.findById(roomId)).isEmpty();
        assertThat(reservationRepository.findById(reservationId)).isEmpty();
        assertThat(reviewRepository.findById(reviewId)).isEmpty();
        assertThat(paymentRepository.findById(paymentId)).isEmpty();
        assertThat(storedImage).doesNotExist();

        mockMvc.perform(get("/admin/properties")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString(title))));
    }

    private RoomEntity createListing(String title, byte[] imageBytes) {
        CreateRoomRequest request = new CreateRoomRequest();
        request.setTitle(title);
        request.setDescription("Listing created for property deletion verification.");
        request.setTypeId(roomTypeRepository.findAll().get(0).getId());
        request.setOvernightPrice(new BigDecimal("5200.00"));
        request.setExtraPersonPrice(new BigDecimal("450.00"));
        request.setMaxPeople(4);
        request.setMinOvernights(2);
        request.setBeds(2);
        request.setBathrooms(1);
        request.setBedrooms(2);
        request.setSquareMeters(84);
        request.setFormattedAddress("88 Delete Road, Pune, India");
        request.setLocality("Pune");
        request.setCountry("India");
        request.setPostalCode("411001");
        request.setNeighborhood("Delete District");
        request.setHouseRulesText("Leave the property in good condition.");
        request.setAmenityIds(List.of(amenityRepository.findAll().get(0).getId()));
        request.setRuleIds(List.of(ruleRepository.findAll().get(0).getId()));
        request.setTransportIds(List.of(transportRepository.findAll().get(0).getId()));

        MockMultipartFile image = new MockMultipartFile(
                "images",
                title.toLowerCase().replace(' ', '-') + ".png",
                "image/png",
                imageBytes);

        return roomService.createRoom(
                userRepository.findByUsername("landlord").orElseThrow(),
                request,
                new MockMultipartFile[] { image });
    }

    private Path toStoredImagePath(String imageUrl) {
        return PROPERTY_UPLOAD_DIR.resolve(imageUrl.substring(imageUrl.lastIndexOf('/') + 1));
    }
}
