package com.harsh.rentify.dto.response;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AdminDashboardResponse {

    private long totalUsers;
    private long totalLandlords;
    private long totalTenants;
    private long totalAdmins;
    private long totalRooms;
    private long totalReservations;
    private long pendingLandlords;
    private BigDecimal totalRevenue = BigDecimal.ZERO;
    private List<String> revenueTrendLabels = new ArrayList<>();
    private List<BigDecimal> revenueTrendValues = new ArrayList<>();
    private List<UserSummaryResponse> landlordsAwaitingApproval = new ArrayList<>();
    private List<UserSummaryResponse> landlords = new ArrayList<>();
    private List<UserSummaryResponse> tenants = new ArrayList<>();

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalLandlords() {
        return totalLandlords;
    }

    public void setTotalLandlords(long totalLandlords) {
        this.totalLandlords = totalLandlords;
    }

    public long getTotalTenants() {
        return totalTenants;
    }

    public void setTotalTenants(long totalTenants) {
        this.totalTenants = totalTenants;
    }

    public long getTotalAdmins() {
        return totalAdmins;
    }

    public void setTotalAdmins(long totalAdmins) {
        this.totalAdmins = totalAdmins;
    }

    public long getTotalRooms() {
        return totalRooms;
    }

    public void setTotalRooms(long totalRooms) {
        this.totalRooms = totalRooms;
    }

    public long getTotalReservations() {
        return totalReservations;
    }

    public void setTotalReservations(long totalReservations) {
        this.totalReservations = totalReservations;
    }

    public long getPendingLandlords() {
        return pendingLandlords;
    }

    public void setPendingLandlords(long pendingLandlords) {
        this.pendingLandlords = pendingLandlords;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public List<String> getRevenueTrendLabels() {
        return revenueTrendLabels;
    }

    public void setRevenueTrendLabels(List<String> revenueTrendLabels) {
        this.revenueTrendLabels = revenueTrendLabels;
    }

    public List<BigDecimal> getRevenueTrendValues() {
        return revenueTrendValues;
    }

    public void setRevenueTrendValues(List<BigDecimal> revenueTrendValues) {
        this.revenueTrendValues = revenueTrendValues;
    }

    public List<UserSummaryResponse> getLandlordsAwaitingApproval() {
        return landlordsAwaitingApproval;
    }

    public void setLandlordsAwaitingApproval(List<UserSummaryResponse> landlordsAwaitingApproval) {
        this.landlordsAwaitingApproval = landlordsAwaitingApproval;
    }

    public List<UserSummaryResponse> getLandlords() {
        return landlords;
    }

    public void setLandlords(List<UserSummaryResponse> landlords) {
        this.landlords = landlords;
    }

    public List<UserSummaryResponse> getTenants() {
        return tenants;
    }

    public void setTenants(List<UserSummaryResponse> tenants) {
        this.tenants = tenants;
    }
}
