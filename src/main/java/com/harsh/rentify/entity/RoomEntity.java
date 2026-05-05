package com.harsh.rentify.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Lob;
import jakarta.persistence.OrderBy;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "rooms")
public class RoomEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    private Integer squareMeters;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal overnightPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal extraPersonPrice;

    @Column(nullable = false)
    private Integer maxPeople;

    @Column(nullable = false)
    private Integer minOvernights;

    @Column(nullable = false)
    private Integer beds;

    @Column(nullable = false)
    private Integer bathrooms;

    @Column(nullable = false)
    private Integer bedrooms;

    @Column(length = 500)
    private String neighborhood;

    @Column(length = 1000)
    private String houseRulesText;

    @Column(length = 500)
    private String primaryImageUrl;

    @Lob
    @Column(name = "image", columnDefinition = "MEDIUMBLOB")
    private byte[] image;

    @Column(length = 100)
    private String imageContentType;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private UserEntity host;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    private RoomTypeEntity type;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "location_id")
    private LocationEntity location;

    @ManyToMany
    @JoinTable(
            name = "room_amenities",
            joinColumns = @JoinColumn(name = "room_id"),
            inverseJoinColumns = @JoinColumn(name = "amenity_id")
    )
    private Set<AmenityEntity> amenities = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(
            name = "room_rules",
            joinColumns = @JoinColumn(name = "room_id"),
            inverseJoinColumns = @JoinColumn(name = "rule_id")
    )
    private Set<RuleEntity> rules = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(
            name = "room_transports",
            joinColumns = @JoinColumn(name = "room_id"),
            inverseJoinColumns = @JoinColumn(name = "transport_id")
    )
    private Set<TransportEntity> transports = new LinkedHashSet<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ReservationEntity> reservations = new LinkedHashSet<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ReviewEntity> reviews = new LinkedHashSet<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<PropertyImageEntity> images = new ArrayList<>();

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSquareMeters() {
        return squareMeters;
    }

    public void setSquareMeters(Integer squareMeters) {
        this.squareMeters = squareMeters;
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

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getImageContentType() {
        return imageContentType;
    }

    public void setImageContentType(String imageContentType) {
        this.imageContentType = imageContentType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public UserEntity getHost() {
        return host;
    }

    public void setHost(UserEntity host) {
        this.host = host;
    }

    public RoomTypeEntity getType() {
        return type;
    }

    public void setType(RoomTypeEntity type) {
        this.type = type;
    }

    public LocationEntity getLocation() {
        return location;
    }

    public void setLocation(LocationEntity location) {
        this.location = location;
    }

    public Set<AmenityEntity> getAmenities() {
        return amenities;
    }

    public void setAmenities(Set<AmenityEntity> amenities) {
        this.amenities = amenities;
    }

    public Set<RuleEntity> getRules() {
        return rules;
    }

    public void setRules(Set<RuleEntity> rules) {
        this.rules = rules;
    }

    public Set<TransportEntity> getTransports() {
        return transports;
    }

    public void setTransports(Set<TransportEntity> transports) {
        this.transports = transports;
    }

    public Set<ReservationEntity> getReservations() {
        return reservations;
    }

    public void setReservations(Set<ReservationEntity> reservations) {
        this.reservations = reservations;
    }

    public Set<ReviewEntity> getReviews() {
        return reviews;
    }

    public void setReviews(Set<ReviewEntity> reviews) {
        this.reviews = reviews;
    }

    public List<PropertyImageEntity> getImages() {
        return images;
    }

    public void setImages(List<PropertyImageEntity> images) {
        this.images = images;
    }

    public void addImage(PropertyImageEntity image) {
        images.add(image);
        image.setRoom(this);
    }

    public void removeImage(PropertyImageEntity image) {
        images.remove(image);
        image.setRoom(null);
    }
}
