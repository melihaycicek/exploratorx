package com.exploratorx.common.geo;

import org.springframework.stereotype.Component;

/**
 * Haversine distance calculator.
 * Computes great-circle distance between two geographic points.
 *
 * Formula:
 *   a = sin²(Δlat/2) + cos(lat1) × cos(lat2) × sin²(Δlon/2)
 *   c = 2 × atan2(√a, √(1−a))
 *   d = R × c
 *
 * Earth radius used: 6371 km
 */
@Component
public class HaversineCalculator {

    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Calculate distance in kilometers between two GeoPoints.
     *
     * @param from origin GeoPoint
     * @param to   destination GeoPoint
     * @return distance in kilometers
     */
    public double distanceKm(GeoPoint from, GeoPoint to) {
        return distanceKm(from.getLatitude(), from.getLongitude(),
                          to.getLatitude(), to.getLongitude());
    }

    /**
     * Calculate distance in kilometers between two lat/lon pairs.
     */
    public double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    /**
     * Calculate required speed (km/h) given distance and time difference.
     *
     * @param distanceKm      distance in km
     * @param timeDiffMinutes time difference in minutes
     * @return required speed in km/h, or Double.MAX_VALUE if time is zero
     */
    public double requiredSpeedKmh(double distanceKm, double timeDiffMinutes) {
        if (timeDiffMinutes <= 0) {
            return Double.MAX_VALUE;
        }
        return distanceKm / (timeDiffMinutes / 60.0);
    }
}
