package com.harsh.rentify.dto.response;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class LandlordDashboardResponse {

    private int listingsCount;
    private int totalBookings;
    private int pendingReservations;
    private int approvedReservations;
    private BigDecimal totalRevenue = BigDecimal.ZERO;
    private List<RoomCardResponse> listings = new ArrayList<>();
    private List<ReservationViewResponse> reservations = new ArrayList<>();

    public int getListingsCount() {
        return listingsCount;
    }

    public void setListingsCount(int listingsCount) {
        this.listingsCount = listingsCount;
    }

    public int getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(int totalBookings) {
        this.totalBookings = totalBookings;
    }

    public int getPendingReservations() {
        return pendingReservations;
    }

    public void setPendingReservations(int pendingReservations) {
        this.pendingReservations = pendingReservations;
    }

    public int getApprovedReservations() {
        return approvedReservations;
    }

    public void setApprovedReservations(int approvedReservations) {
        this.approvedReservations = approvedReservations;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public List<RoomCardResponse> getListings() {
        return listings;
    }

    public void setListings(List<RoomCardResponse> listings) {
        this.listings = listings;
    }

    public List<ReservationViewResponse> getReservations() {
        return reservations;
    }

    public void setReservations(List<ReservationViewResponse> reservations) {
        this.reservations = reservations;
    }
}
