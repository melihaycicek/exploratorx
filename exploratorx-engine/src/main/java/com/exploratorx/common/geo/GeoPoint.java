package com.exploratorx.common.geo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a geographic coordinate (latitude, longitude).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeoPoint {

    private double latitude;
    private double longitude;

    @Override
    public String toString() {
        return String.format("GeoPoint{lat=%.4f, lon=%.4f}", latitude, longitude);
    }
}
