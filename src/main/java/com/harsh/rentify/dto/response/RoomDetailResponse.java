package com.harsh.rentify.dto.response;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class RoomDetailResponse {

    private Long id;
    private String title;
    private String type;
    private String description;
    private String location;
    private String neighborhood;
    private String imageUrl;
    private List<String> imageUrls = new ArrayList<>();
    private BigDecimal overnightPrice;
    private BigDecimal extraPersonPrice;
    private Integer maxPeople;
    private Integer minOvernights;
    private Integer beds;
    private Integer bathrooms;
    private Integer bedrooms;
    private Integer squareMeters;
    private Double averageRating;
    private Integer reviewCount;
    private String houseRulesText;
    private UserSummaryResponse host;
    private List<String> amenities = new ArrayList<>();
    private List<String> rules = new ArrayList<>();
    private List<String> transports = new ArrayList<>();
    private List<ReviewViewResponse> reviews = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public BigDecimal getOvernightPrice() {
        return overnightPrice;
    }

    public void setOvernightPrice(BigDecimal overnightPrice) {
        this.overnightPrice = overnightPrice;
    }

    public BigDecimal getExtraPersonPrice() {
        return extraPersonPrice;
    }

    public void setExtraPersonPrice(BigDecimal extraPersonPrice) {
        this.extraPersonPrice = extraPersonPrice;
    }

    public Integer getMaxPeople() {
        return maxPeople;
    }

    public void setMaxPeople(Integer maxPeople) {
        this.maxPeople = maxPeople;
    }

    public Integer getMinOvernights() {
        return minOvernights;
    }

    public void setMinOvernights(Integer minOvernights) {
        this.minOvernights = minOvernights;
    }

    public Integer getBeds() {
        return beds;
    }

    public void setBeds(Integer beds) {
        this.beds = beds;
    }

    public Integer getBathrooms() {
        return bathrooms;
    }

    public void setBathrooms(Integer bathrooms) {
        this.bathrooms = bathrooms;
    }

    public Integer getBedrooms() {
        return bedrooms;
    }

    public void setBedrooms(Integer bedrooms) {
        this.bedrooms = bedrooms;
    }

    public Integer getSquareMeters() {
        return squareMeters;
    }

    public void setSquareMeters(Integer squareMeters) {
        this.squareMeters = squareMeters;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }

    public String getHouseRulesText() {
        return houseRulesText;
    }

    public void setHouseRulesText(String houseRulesText) {
        this.houseRulesText = houseRulesText;
    }

    public UserSummaryResponse getHost() {
        return host;
    }

    public void setHost(UserSummaryResponse host) {
        this.host = host;
    }

    public List<String> getAmenities() {
        return amenities;
    }

    public void setAmenities(List<String> amenities) {
        this.amenities = amenities;
    }

    public List<String> getRules() {
        return rules;
    }

    public void setRules(List<String> rules) {
        this.rules = rules;
    }

    public List<String> getTransports() {
        return transports;
    }

    public void setTransports(List<String> transports) {
        this.transports = transports;
    }

    public List<ReviewViewResponse> getReviews() {
        return reviews;
    }

    public void setReviews(List<ReviewViewResponse> reviews) {
        this.reviews = reviews;
    }
}
