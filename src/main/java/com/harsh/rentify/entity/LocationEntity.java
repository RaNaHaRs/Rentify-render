package com.harsh.rentify.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "locations")
public class LocationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 120)
    private String route;

    @Column(length = 50)
    private String streetNumber;

    @Column(length = 120)
    private String locality;

    @Column(length = 30)
    private String postalCode;

    @Column(length = 120)
    private String country;

    @Column(length = 120)
    private String administrativeAreaLevel1;

    @Column(length = 120)
    private String administrativeAreaLevel2;

    @Column(length = 120)
    private String administrativeAreaLevel3;

    @Column(length = 120)
    private String administrativeAreaLevel4;

    @Column(length = 120)
    private String administrativeAreaLevel5;

    @Column(length = 250)
    private String formattedAddress;

    private Double latitude;

    private Double longitude;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(String streetNumber) {
        this.streetNumber = streetNumber;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getAdministrativeAreaLevel1() {
        return administrativeAreaLevel1;
    }

    public void setAdministrativeAreaLevel1(String administrativeAreaLevel1) {
        this.administrativeAreaLevel1 = administrativeAreaLevel1;
    }

    public String getAdministrativeAreaLevel2() {
        return administrativeAreaLevel2;
    }

    public void setAdministrativeAreaLevel2(String administrativeAreaLevel2) {
        this.administrativeAreaLevel2 = administrativeAreaLevel2;
    }

    public String getAdministrativeAreaLevel3() {
        return administrativeAreaLevel3;
    }

    public void setAdministrativeAreaLevel3(String administrativeAreaLevel3) {
        this.administrativeAreaLevel3 = administrativeAreaLevel3;
    }

    public String getAdministrativeAreaLevel4() {
        return administrativeAreaLevel4;
    }

    public void setAdministrativeAreaLevel4(String administrativeAreaLevel4) {
        this.administrativeAreaLevel4 = administrativeAreaLevel4;
    }

    public String getAdministrativeAreaLevel5() {
        return administrativeAreaLevel5;
    }

    public void setAdministrativeAreaLevel5(String administrativeAreaLevel5) {
        this.administrativeAreaLevel5 = administrativeAreaLevel5;
    }

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public void setFormattedAddress(String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
