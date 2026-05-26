package com.exploratorx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ExploratorX Engine — main entry point.
 * Real-time anomaly exploration engine for telecom (CDR) and payment event streams.
 * Codename: DuruGörü
 *
 * @author Melih Ayçiçek
 * @version 1.0.0
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties
public class ExploratorXApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExploratorXApplication.class, args);
    }
}
