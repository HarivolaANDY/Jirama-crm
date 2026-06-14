package com.jirama.domain.shared;

import java.util.Objects;

/**
 * Value Object representing a physical address.
 */
public final class Address {

    private final String line1;
    private final String line2;
    private final String city;
    private final String district;
    private final String regionCode;
    private final String postalCode;
    private final Double latitude;
    private final Double longitude;

    public Address(String line1, String line2, String city, String district,
                   String regionCode, String postalCode, Double latitude, Double longitude) {
        this.line1 = Objects.requireNonNull(line1, "line1 must not be null");
        this.line2 = line2;
        this.city = Objects.requireNonNull(city, "city must not be null");
        this.district = district;
        this.regionCode = regionCode;
        this.postalCode = postalCode;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Address(String line1, String city, String regionCode) {
        this(line1, null, city, null, regionCode, null, null, null);
    }

    public String getLine1() { return line1; }
    public String getLine2() { return line2; }
    public String getCity() { return city; }
    public String getDistrict() { return district; }
    public String getRegionCode() { return regionCode; }
    public String getPostalCode() { return postalCode; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }

    public String fullAddress() {
        StringBuilder sb = new StringBuilder(line1);
        if (line2 != null) sb.append(", ").append(line2);
        sb.append(", ").append(city);
        if (district != null) sb.append(", ").append(district);
        if (regionCode != null) sb.append(", ").append(regionCode);
        if (postalCode != null) sb.append(" ").append(postalCode);
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Address address)) return false;
        return Objects.equals(line1, address.line1) &&
                Objects.equals(city, address.city) &&
                Objects.equals(regionCode, address.regionCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(line1, city, regionCode);
    }

    @Override
    public String toString() {
        return fullAddress();
    }
}
