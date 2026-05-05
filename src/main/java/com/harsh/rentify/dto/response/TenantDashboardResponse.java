package com.harsh.rentify.dto.response;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TenantDashboardResponse {

    private int reservationsCount;
    private int upcomingReservations;
    private int approvedReservations;
    private int cancelledReservations;
    private BigDecimal totalSpend = BigDecimal.ZERO;
    private List<ReservationViewResponse> reservations = new ArrayList<>();
    private List<RoomCardResponse> availableListings = new ArrayList<>();

    public int getReservationsCount() {
        return reservationsCount;
    }

    public void setReservationsCount(int reservationsCount) {
        this.reservationsCount = reservationsCount;
    }

    public int getUpcomingReservations() {
        return upcomingReservations;
    }

    public void setUpcomingReservations(int upcomingReservations) {
        this.upcomingReservations = upcomingReservations;
    }

    public int getApprovedReservations() {
        return approvedReservations;
    }

    public void setApprovedReservations(int approvedReservations) {
        this.approvedReservations = approvedReservations;
    }

    public int getCancelledReservations() {
        return cancelledReservations;
    }

    public void setCancelledReservations(int cancelledReservations) {
        this.cancelledReservations = cancelledReservations;
    }

    public BigDecimal getTotalSpend() {
        return totalSpend;
    }

    public void setTotalSpend(BigDecimal totalSpend) {
        this.totalSpend = totalSpend;
    }

    public List<ReservationViewResponse> getReservations() {
        return reservations;
    }

    public void setReservations(List<ReservationViewResponse> reservations) {
        this.reservations = reservations;
    }

    public List<RoomCardResponse> getAvailableListings() {
        return availableListings;
    }

    public void setAvailableListings(List<RoomCardResponse> availableListings) {
        this.availableListings = availableListings;
    }
}
