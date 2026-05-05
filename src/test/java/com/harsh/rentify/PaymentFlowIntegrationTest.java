package com.harsh.rentify;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.harsh.rentify.dto.request.BookingRequest;
import com.harsh.rentify.dto.request.CreateRoomRequest;
import com.harsh.rentify.dto.response.ReservationViewResponse;
import com.harsh.rentify.entity.PaymentEntity;
import com.harsh.rentify.entity.PaymentStatus;
import com.harsh.rentify.entity.ReservationStatus;
import com.harsh.rentify.entity.RoomEntity;
import com.harsh.rentify.entity.UserEntity;
import com.harsh.rentify.repository.PaymentRepository;
import com.harsh.rentify.repository.ReservationRepository;
import com.harsh.rentify.repository.RoomRepository;
import com.harsh.rentify.repository.RoomTypeRepository;
import com.harsh.rentify.repository.UserRepository;
import com.harsh.rentify.service.ReservationService;
import com.harsh.rentify.service.RoomService;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoomService roomService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void tenantCanPayApprovedReservationOnlyOnce() throws Exception {
        UserEntity landlord = userRepository.findByUsername("landlord").orElseThrow();
        UserEntity tenant = userRepository.findByUsername("tenant").orElseThrow();
        Long roomId = ensurePaymentRoomExists(landlord);
        Long approvedBookingId = ensureReservation(roomId, tenant, landlord, LocalDate.of(2032, 1, 10), true);
        Long pendingBookingId = ensureReservation(roomId, tenant, landlord, LocalDate.of(2032, 2, 10), false);

        mockMvc.perform(get("/tenant/bookings")
                        .with(user("tenant").roles("TENANT")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/bookings/cancel/" + approvedBookingId)))
                .andExpect(content().string(containsString("/bookings/cancel/" + pendingBookingId)))
                .andExpect(content().string(containsString("/payments/" + approvedBookingId)))
                .andExpect(content().string(not(containsString("/payments/" + pendingBookingId))));

        mockMvc.perform(get("/tenant/payments")
                        .with(user("tenant").roles("TENANT")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/payments/" + approvedBookingId)))
                .andExpect(content().string(not(containsString("/payments/" + pendingBookingId))));

        mockMvc.perform(get("/payments/" + approvedBookingId)
                        .with(user("tenant").roles("TENANT")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Complete your booking payment.")))
                .andExpect(content().string(containsString("Payment Flow Listing")))
                .andExpect(content().string(containsString("Mira Kapoor")))
                .andExpect(content().string(containsString("Arjun Shah")));

        long initialPayments = paymentRepository.count();
        BigDecimal tenantSpendBefore = nullToZero(paymentRepository.sumAmountByTenantId(tenant.getId()));
        BigDecimal landlordRevenueBefore = nullToZero(paymentRepository.sumAmountByLandlordId(landlord.getId()));
        BigDecimal platformRevenueBefore = nullToZero(paymentRepository.sumAllAmounts());

        mockMvc.perform(post("/payments")
                        .param("bookingId", approvedBookingId.toString())
                        .with(user("tenant").roles("TENANT"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("successMessage", "Payment successful!"))
                .andExpect(redirectedUrl("/tenant/payments"));

        assertThat(paymentRepository.count()).isEqualTo(initialPayments + 1);

        PaymentEntity payment = paymentRepository.findByBookingId(approvedBookingId).orElseThrow();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(payment.getAmount()).isEqualByComparingTo("13200.00");

        BigDecimal tenantSpendAfter = nullToZero(paymentRepository.sumAmountByTenantId(tenant.getId()));
        BigDecimal landlordRevenueAfter = nullToZero(paymentRepository.sumAmountByLandlordId(landlord.getId()));
        BigDecimal platformRevenueAfter = nullToZero(paymentRepository.sumAllAmounts());

        assertThat(tenantSpendAfter).isEqualByComparingTo(tenantSpendBefore.add(payment.getAmount()));
        assertThat(landlordRevenueAfter).isEqualByComparingTo(landlordRevenueBefore.add(payment.getAmount()));
        assertThat(platformRevenueAfter).isEqualByComparingTo(platformRevenueBefore.add(payment.getAmount()));

        ReservationViewResponse paidReservation = reservationService.getTenantReservations(tenant).stream()
                .filter(reservation -> reservation.getId().equals(approvedBookingId))
                .findFirst()
                .orElseThrow();
        assertThat(paidReservation.isPaid()).isTrue();

        String formattedTenantSpend = formatCurrency(tenantSpendAfter);
        String formattedLandlordRevenue = formatCurrency(landlordRevenueAfter);
        String formattedPlatformRevenue = formatCurrency(platformRevenueAfter);

        mockMvc.perform(get("/tenant/payments")
                        .flashAttr("successMessage", "Payment successful!")
                        .with(user("tenant").roles("TENANT")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Payment successful!")))
                .andExpect(content().string(not(containsString("/payments/" + approvedBookingId))))
                .andExpect(content().string(containsString("Payment completed")));

        mockMvc.perform(get("/tenant/dashboard")
                        .with(user("tenant").roles("TENANT")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(formattedTenantSpend)))
                .andExpect(content().string(containsString("Total Spending")))
                .andExpect(content().string(containsString("Approved Bookings")));

        mockMvc.perform(post("/bookings/cancel/" + approvedBookingId)
                        .with(user("tenant").roles("TENANT"))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Paid bookings cannot be cancelled.")));

        mockMvc.perform(get("/landlord/dashboard")
                        .with(user("landlord").roles("LANDLORD")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(formattedLandlordRevenue)))
                .andExpect(content().string(containsString("Total Revenue")));

        mockMvc.perform(get("/admin/dashboard")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(formattedPlatformRevenue)))
                .andExpect(content().string(containsString("Total Users")))
                .andExpect(content().string(containsString("Total Properties")))
                .andExpect(content().string(containsString("Total Bookings")));
    }

    @Test
    void tenantCanCancelPendingOrApprovedReservationAndPaymentGetsDisabled() throws Exception {
        UserEntity landlord = userRepository.findByUsername("landlord").orElseThrow();
        UserEntity tenant = userRepository.findByUsername("tenant").orElseThrow();
        Long roomId = ensurePaymentRoomExists(landlord);
        Long pendingBookingId = ensureReservation(roomId, tenant, landlord, LocalDate.of(2032, 3, 10), false);
        Long approvedBookingId = ensureReservation(roomId, tenant, landlord, LocalDate.of(2032, 4, 10), true);

        mockMvc.perform(get("/tenant/bookings")
                        .with(user("tenant").roles("TENANT")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/bookings/cancel/" + pendingBookingId)))
                .andExpect(content().string(containsString("/bookings/cancel/" + approvedBookingId)))
                .andExpect(content().string(containsString("/payments/" + approvedBookingId)));

        mockMvc.perform(post("/bookings/cancel/" + approvedBookingId)
                        .with(user("tenant").roles("TENANT"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("successMessage", "Booking cancelled successfully"))
                .andExpect(redirectedUrl("/tenant/bookings"));

        assertThat(reservationRepository.findById(approvedBookingId)).isPresent();
        assertThat(reservationRepository.findById(approvedBookingId).orElseThrow().getStatus())
                .isEqualTo(ReservationStatus.CANCELLED);

        mockMvc.perform(get("/tenant/bookings")
                        .flashAttr("successMessage", "Booking cancelled successfully")
                        .with(user("tenant").roles("TENANT")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Booking cancelled successfully")))
                .andExpect(content().string(containsString("CANCELLED")))
                .andExpect(content().string(not(containsString("/payments/" + approvedBookingId))))
                .andExpect(content().string(not(containsString("/bookings/cancel/" + approvedBookingId))))
                .andExpect(content().string(containsString("/bookings/cancel/" + pendingBookingId)));

        mockMvc.perform(get("/tenant/payments")
                        .with(user("tenant").roles("TENANT")))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("/payments/" + approvedBookingId))));

        mockMvc.perform(get("/payments/" + approvedBookingId)
                        .with(user("tenant").roles("TENANT")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Payment is only available for approved bookings.")));
    }

    private Long ensurePaymentRoomExists(UserEntity landlord) {
        return roomRepository.findAll().stream()
                .filter(room -> "Payment Flow Listing".equals(room.getTitle()))
                .findFirst()
                .map(RoomEntity::getId)
                .orElseGet(() -> {
                    CreateRoomRequest request = new CreateRoomRequest();
                    request.setTitle("Payment Flow Listing");
                    request.setDescription("Listing used for payment flow verification.");
                    request.setTypeId(roomTypeRepository.findAll().get(0).getId());
                    request.setOvernightPrice(new BigDecimal("4100.00"));
                    request.setExtraPersonPrice(new BigDecimal("300.00"));
                    request.setMaxPeople(2);
                    request.setMinOvernights(1);
                    request.setBeds(1);
                    request.setBathrooms(1);
                    request.setBedrooms(1);
                    request.setSquareMeters(48);
                    request.setFormattedAddress("12 Payment Road, Mumbai, India");
                    request.setLocality("Mumbai");
                    request.setCountry("India");
                    request.setPostalCode("400001");
                    request.setNeighborhood("Colaba");
                    request.setHouseRulesText("Standard host rules apply.");
                    return roomService.createRoom(landlord, request, null).getId();
                });
    }

    private Long ensureReservation(
            Long roomId,
            UserEntity tenant,
            UserEntity landlord,
            LocalDate checkInDate,
            boolean approved
    ) {
        ReservationViewResponse existingReservation = reservationService.getTenantReservations(tenant).stream()
                .filter(reservation -> reservation.getRoomId().equals(roomId))
                .filter(reservation -> reservation.getCheckInDate().equals(checkInDate))
                .findFirst()
                .orElse(null);

        if (existingReservation != null) {
            return existingReservation.getId();
        }

        BookingRequest request = new BookingRequest();
        request.setCheckInDate(checkInDate);
        request.setCheckOutDate(checkInDate.plusDays(3));
        request.setGuests(2);

        ReservationViewResponse createdReservation = reservationService.createReservation(roomId, request, tenant);
        if (approved) {
            reservationService.approveReservation(createdReservation.getId(), landlord);
        }
        return createdReservation.getId();
    }

    private String formatCurrency(BigDecimal amount) {
        return "INR " + new DecimalFormat("#,##0.00").format(amount);
    }

    private BigDecimal nullToZero(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }
}
