package com.exploratorx.cdr.demo;

import com.exploratorx.cdr.enums.SignalType;
import com.exploratorx.cdr.model.CdrSignal;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

/**
 * Factory for generating synthetic German CDR signals.
 *
 * Uses real German city coordinates for realistic demonstration scenarios.
 * All data is purely synthetic — no real subscriber information.
 */
@Component
public class GermanySignalFactory {

    /** German city coordinate dataset (synthetic, for demo only). */
    public enum GermanyCity {
        BERLIN("Berlin", 52.5200, 13.4050),
        HAMBURG("Hamburg", 53.5511, 9.9937),
        MUNICH("Munich", 48.1351, 11.5820),
        FRANKFURT("Frankfurt", 50.1109, 8.6821),
        COLOGNE("Cologne", 50.9333, 6.9500),
        STUTTGART("Stuttgart", 48.7758, 9.1829),
        LEIPZIG("Leipzig", 51.3397, 12.3731),
        DUSSELDORF("Düsseldorf", 51.2217, 6.7762),
        BREMEN("Bremen", 53.0793, 8.8017),
        HANNOVER("Hannover", 52.3759, 9.7320);

        public final String name;
        public final double lat;
        public final double lon;

        GermanyCity(String name, double lat, double lon) {
            this.name = name;
            this.lat = lat;
            this.lon = lon;
        }
    }

    private static final Random RANDOM = new Random();

    /**
     * Create a normal CDR signal for a given subscriber in a given city.
     */
    public CdrSignal normalSignal(String subscriberId, GermanyCity city) {
        return CdrSignal.builder()
                .subscriberId(subscriberId)
                .eventTime(Instant.now())
                .city(city.name)
                .latitude(city.lat + (RANDOM.nextDouble() - 0.5) * 0.01)
                .longitude(city.lon + (RANDOM.nextDouble() - 0.5) * 0.01)
                .cellId("CELL-" + city.name.toUpperCase().substring(0, 3) + "-" + RANDOM.nextInt(999))
                .signalType(SignalType.values()[RANDOM.nextInt(SignalType.values().length)])
                .build();
    }

    /**
     * Create an impossible travel scenario: subscriber jumps Berlin → Hamburg instantly.
     */
    public List<CdrSignal> impossibleScenario(String subscriberId) {
        List<CdrSignal> signals = new ArrayList<>();
        // Signal 1: Berlin
        signals.add(normalSignal(subscriberId, GermanyCity.BERLIN));
        // Signal 2: Hamburg, 1 minute later (impossible: 288km in 1 min)
        signals.add(CdrSignal.builder()
                .subscriberId(subscriberId)
                .eventTime(Instant.now().plusSeconds(60))
                .city(GermanyCity.HAMBURG.name)
                .latitude(GermanyCity.HAMBURG.lat)
                .longitude(GermanyCity.HAMBURG.lon)
                .cellId("CELL-HAM-001")
                .signalType(SignalType.VOICE)
                .build());
        return signals;
    }

    /**
     * Create a suspicious movement scenario: subscriber Berlin → Frankfurt in 15 minutes.
     * Distance ~550km, requires ~2200 km/h (suspicious but not max impossible).
     */
    public List<CdrSignal> suspiciousScenario(String subscriberId) {
        List<CdrSignal> signals = new ArrayList<>();
        signals.add(normalSignal(subscriberId, GermanyCity.BERLIN));
        signals.add(CdrSignal.builder()
                .subscriberId(subscriberId)
                .eventTime(Instant.now().plusSeconds(15 * 60))
                .city(GermanyCity.FRANKFURT.name)
                .latitude(GermanyCity.FRANKFURT.lat)
                .longitude(GermanyCity.FRANKFURT.lon)
                .cellId("CELL-FRA-042")
                .signalType(SignalType.DATA)
                .build());
        return signals;
    }

    /**
     * Create a split signal scenario: same subscriber in two cities within 30 seconds.
     */
    public List<CdrSignal> splitSignalScenario(String subscriberId) {
        List<CdrSignal> signals = new ArrayList<>();
        Instant base = Instant.now();
        signals.add(CdrSignal.builder()
                .subscriberId(subscriberId)
                .eventTime(base)
                .city(GermanyCity.BERLIN.name)
                .latitude(GermanyCity.BERLIN.lat)
                .longitude(GermanyCity.BERLIN.lon)
                .cellId("CELL-BER-010")
                .signalType(SignalType.SMS)
                .build());
        signals.add(CdrSignal.builder()
                .subscriberId(subscriberId)
                .eventTime(base.plusSeconds(30))
                .city(GermanyCity.MUNICH.name)
                .latitude(GermanyCity.MUNICH.lat)
                .longitude(GermanyCity.MUNICH.lon)
                .cellId("CELL-MUC-007")
                .signalType(SignalType.VOICE)
                .build());
        return signals;
    }

    /** Generate a synthetic subscriber ID. */
    public String randomSubscriberId() {
        return "SUB-DE-" + String.format("%06d", RANDOM.nextInt(999999));
    }

    /** Pick a random German city. */
    public GermanyCity randomCity() {
        GermanyCity[] cities = GermanyCity.values();
        return cities[RANDOM.nextInt(cities.length)];
    }
}
