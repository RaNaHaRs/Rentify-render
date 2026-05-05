package com.harsh.rentify.service;

import com.harsh.rentify.dto.response.AppNotificationResponse;
import com.harsh.rentify.entity.ReservationEntity;
import com.harsh.rentify.entity.ReservationStatus;
import com.harsh.rentify.entity.Role;
import com.harsh.rentify.entity.RoomEntity;
import com.harsh.rentify.entity.UserEntity;
import com.harsh.rentify.mapper.UserMapper;
import com.harsh.rentify.repository.PaymentRepository;
import com.harsh.rentify.repository.ReservationRepository;
import com.harsh.rentify.repository.RoomRepository;
import com.harsh.rentify.repository.UserRepository;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {
    private static final int MAX_NOTIFICATIONS = 8;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH);

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public NotificationService(
            RoomRepository roomRepository,
            ReservationRepository reservationRepository,
            PaymentRepository paymentRepository,
            UserRepository userRepository,
            UserMapper userMapper
    ) {
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Transactional(readOnly = true)
    public List<AppNotificationResponse> getNotificationsFor(UserEntity user) {
        List<AppNotificationResponse> notifications = switch (user.getRole()) {
            case TENANT -> buildTenantNotifications(user);
            case LANDLORD -> buildLandlordNotifications(user);
            case ADMIN -> buildAdminNotifications();
        };

        return notifications.stream()
                .sorted(Comparator.comparing(
                        AppNotificationResponse::getCreatedAt,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                        .reversed())
                .limit(MAX_NOTIFICATIONS)
                .toList();
    }

    private List<AppNotificationResponse> buildTenantNotifications(UserEntity tenant) {
        List<AppNotificationResponse> notifications = new ArrayList<>();

        roomRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .filter(room -> room.getHost() != null && !room.getHost().getId().equals(tenant.getId()))
                .limit(3)
                .forEach(room -> notifications.add(buildNotification(
                        "tenant-property-" + room.getId(),
                        "New property added",
                        "New property added! " + room.getTitle() + " is now available in " + resolveRoomLocation(room) + ".",
                        "info",
                        "/tenant/properties",
                        room.getCreatedAt()
                )));

        reservationRepository.findByTenantOrderByCreatedAtDesc(tenant).stream()
                .filter(reservation -> reservation.getStatus() == ReservationStatus.APPROVED)
                .limit(2)
                .forEach(reservation -> notifications.add(buildNotification(
                        "tenant-approved-" + reservation.getId(),
                        "Booking approved",
                        "Your booking approved! " + reservation.getRoom().getTitle()
                                + " is confirmed for " + reservation.getCheckInDate().format(DATE_FORMATTER) + ".",
                        "success",
                        "/tenant/bookings",
                        reservation.getCreatedAt()
                )));

        reservationRepository.findByTenantOrderByCreatedAtDesc(tenant).stream()
                .filter(reservation -> reservation.getStatus() == ReservationStatus.APPROVED && !reservation.isPaid())
                .limit(2)
                .forEach(reservation -> notifications.add(buildNotification(
                        "tenant-payment-" + reservation.getId(),
                        "Payment request available",
                        "Payment request available for " + reservation.getRoom().getTitle()
                                + ". Complete your payment to secure the stay.",
                        "accent",
                        "/tenant/payments",
                        reservation.getCreatedAt()
                )));

        return notifications;
    }

    private List<AppNotificationResponse> buildLandlordNotifications(UserEntity landlord) {
        List<AppNotificationResponse> notifications = new ArrayList<>();

        reservationRepository.findByRoomHostOrderByCreatedAtDesc(landlord).stream()
                .filter(reservation -> reservation.getStatus() == ReservationStatus.PENDING)
                .limit(4)
                .forEach(reservation -> notifications.add(buildNotification(
                        "landlord-booking-" + reservation.getId(),
                        "New booking request",
                        "New booking request! " + resolveTenantName(reservation.getTenant())
                                + " wants to book " + reservation.getRoom().getTitle() + ".",
                        "info",
                        "/landlord/bookings",
                        reservation.getCreatedAt()
                )));

        paymentRepository.findAll(Sort.by(Sort.Direction.DESC, "paymentDate")).stream()
                .filter(payment -> payment.getBooking() != null
                        && payment.getBooking().getRoom() != null
                        && payment.getBooking().getRoom().getHost() != null
                        && payment.getBooking().getRoom().getHost().getId().equals(landlord.getId()))
                .limit(3)
                .forEach(payment -> notifications.add(buildNotification(
                        "landlord-payment-" + payment.getId(),
                        "Payment received",
                        "Payment received! INR " + formatAmount(payment.getAmount())
                                + " arrived for " + payment.getBooking().getRoom().getTitle() + ".",
                        "success",
                        "/landlord/dashboard",
                        payment.getPaymentDate()
                )));

        return notifications;
    }

    private List<AppNotificationResponse> buildAdminNotifications() {
        List<AppNotificationResponse> notifications = new ArrayList<>();

        userRepository.findByRoleAndHostConfirmedFalseOrderByCreatedAtAsc(Role.LANDLORD).stream()
                .limit(3)
                .forEach(user -> notifications.add(buildNotification(
                        "admin-landlord-" + user.getId(),
                        "Landlord approval pending",
                        "Landlord approval pending for " + resolveUserName(user) + ".",
                        "accent",
                        "/admin/users",
                        user.getCreatedAt()
                )));

        reservationRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .limit(3)
                .forEach(reservation -> notifications.add(buildNotification(
                        "admin-booking-" + reservation.getId(),
                        "Recent booking activity",
                        resolveTenantName(reservation.getTenant()) + " placed a "
                                + reservation.getStatus().name().toLowerCase(Locale.ENGLISH)
                                + " booking for " + reservation.getRoom().getTitle() + ".",
                        "info",
                        "/admin/bookings",
                        reservation.getCreatedAt()
                )));

        paymentRepository.findAll(Sort.by(Sort.Direction.DESC, "paymentDate")).stream()
                .limit(2)
                .forEach(payment -> notifications.add(buildNotification(
                        "admin-payment-" + payment.getId(),
                        "Platform payment received",
                        "Payment of INR " + formatAmount(payment.getAmount())
                                + " was recorded for " + payment.getBooking().getRoom().getTitle() + ".",
                        "success",
                        "/admin/dashboard",
                        payment.getPaymentDate()
                )));

        return notifications;
    }

    private AppNotificationResponse buildNotification(
            String id,
            String title,
            String message,
            String level,
            String link,
            LocalDateTime createdAt
    ) {
        AppNotificationResponse notification = new AppNotificationResponse();
        notification.setId(id);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setLevel(level);
        notification.setLink(link);
        notification.setCreatedAt(createdAt);
        notification.setRelativeTime(formatRelativeTime(createdAt));
        return notification;
    }

    private String resolveRoomLocation(RoomEntity room) {
        if (room.getLocation() != null && room.getLocation().getLocality() != null && !room.getLocation().getLocality().isBlank()) {
            return room.getLocation().getLocality();
        }
        if (room.getLocation() != null && room.getLocation().getFormattedAddress() != null && !room.getLocation().getFormattedAddress().isBlank()) {
            return room.getLocation().getFormattedAddress();
        }
        return "your area";
    }

    private String resolveUserName(UserEntity user) {
        return user == null ? "User" : userMapper.resolveDisplayName(user);
    }

    private String resolveTenantName(UserEntity tenant) {
        return tenant == null ? "A tenant" : resolveUserName(tenant);
    }

    private String formatAmount(BigDecimal amount) {
        return amount == null ? "0.00" : amount.stripTrailingZeros().toPlainString();
    }

    private String formatRelativeTime(LocalDateTime timestamp) {
        if (timestamp == null) {
            return "Recently";
        }

        Duration age = Duration.between(timestamp, LocalDateTime.now());
        if (age.isNegative() || age.toMinutes() < 1) {
            return "Just now";
        }
        if (age.toHours() < 1) {
            return age.toMinutes() + " min ago";
        }
        if (age.toDays() < 1) {
            return age.toHours() + " hr ago";
        }
        if (age.toDays() < 30) {
            return age.toDays() + " day" + (age.toDays() == 1 ? "" : "s") + " ago";
        }
        return timestamp.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH));
    }
}
