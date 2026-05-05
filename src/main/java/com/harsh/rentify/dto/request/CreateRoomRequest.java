package com.harsh.rentify.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CreateRoomRequest {

    @NotBlank(message = "Title is required.")
    @Size(max = 150, message = "Title is too long.")
    private String title;

    @NotBlank(message = "Description is required.")
    @Size(max = 2000, message = "Description is too long.")
    private String description;

    @NotNull(message = "Select a property type.")
    private Long typeId;

    @NotNull(message = "Nightly price is required.")
    @DecimalMin(value = "1.0", message = "Nightly price must be greater than zero.")
    private BigDecimal overnightPrice;

    @NotNull(message = "Extra guest price is required.")
    @DecimalMin(value = "0.0", message = "Extra guest price cannot be negative.")
    private BigDecimal extraPersonPrice;

    @NotNull(message = "Maximum guests is required.")
    @Min(value = 1, message = "Maximum guests must be at least 1.")
    private Integer maxPeople;

    @NotNull(message = "Minimum nights is required.")
    @Min(value = 1, message = "Minimum nights must be at least 1.")
    private Integer minOvernights;

    @NotNull(message = "Beds count is required.")
    @Min(value = 1, message = "Beds must be at least 1.")
    private Integer beds;

    @NotNull(message = "Bathrooms count is required.")
    @Min(value = 1, message = "Bathrooms must be at least 1.")
    private Integer bathrooms;

    @NotNull(message = "Bedrooms count is required.")
    @Min(value = 1, message = "Bedrooms must be at least 1.")
    private Integer bedrooms;

    @Min(value = 1, message = "Area must be positive.")
    private Integer squareMeters;

    @Size(max = 500, message = "Neighborhood is too long.")
    private String neighborhood;

    @Size(max = 1000, message = "House rules are too long.")
    private String houseRulesText;

    @Size(max = 500, message = "Image URL is too long.")
    private String primaryImageUrl;

    @NotBlank(message = "Address is required.")
    @Size(max = 250, message = "Address is too long.")
    private String formattedAddress;

    @Size(max = 120, message = "City is too long.")
    private String locality;

    @Size(max = 120, message = "Country is too long.")
    private String country;

    @Size(max = 30, message = "Postal code is too long.")
    private String postalCode;

    private List<Long> amenityIds = new ArrayList<>();
    private List<Long> ruleIds = new ArrayList<>();
    private List<Long> transportIds = new ArrayList<>();

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
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

    public String getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }

    public String getHouseRulesText() {
        return houseRulesText;
    }

    public void setHouseRulesText(String houseRulesText) {
        this.houseRulesText = houseRulesText;
    }

    public String getPrimaryImageUrl() {
        return primaryImageUrl;
    }

    public void setPrimaryImageUrl(String primaryImageUrl) {
        this.primaryImageUrl = primaryImageUrl;
    }

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public void setFormattedAddress(String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public List<Long> getAmenityIds() {
        return amenityIds;
    }

    public void setAmenityIds(List<Long> amenityIds) {
        this.amenityIds = amenityIds;
    }

    public List<Long> getRuleIds() {
        return ruleIds;
    }

    public void setRuleIds(List<Long> ruleIds) {
        this.ruleIds = ruleIds;
    }

    public List<Long> getTransportIds() {
        return transportIds;
    }

    public void setTransportIds(List<Long> transportIds) {
        this.transportIds = transportIds;
    }
}
