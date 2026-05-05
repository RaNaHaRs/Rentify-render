package com.harsh.rentify.dto.response;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProfileViewResponse {

    private UserSummaryResponse user;
    private String firstName;
    private String lastName;
    private LocalDate birthday;
    private String phone;
    private String about;
    private LocationViewResponse location;
    private int listingsCount;
    private int reservationsCount;
    private List<ProfileCommentResponse> comments = new ArrayList<>();

    public UserSummaryResponse getUser() {
        return user;
    }

    public void setUser(UserSummaryResponse user) {
        this.user = user;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public LocationViewResponse getLocation() {
        return location;
    }

    public void setLocation(LocationViewResponse location) {
        this.location = location;
    }

    public int getListingsCount() {
        return listingsCount;
    }

    public void setListingsCount(int listingsCount) {
        this.listingsCount = listingsCount;
    }

    public int getReservationsCount() {
        return reservationsCount;
    }

    public void setReservationsCount(int reservationsCount) {
        this.reservationsCount = reservationsCount;
    }

    public List<ProfileCommentResponse> getComments() {
        return comments;
    }

    public void setComments(List<ProfileCommentResponse> comments) {
        this.comments = comments;
    }
}
