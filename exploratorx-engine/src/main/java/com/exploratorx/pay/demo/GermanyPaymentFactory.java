package com.exploratorx.pay.demo;

import com.exploratorx.pay.enums.PaymentChannel;
import com.exploratorx.pay.model.PaymentTransaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Factory for generating synthetic German payment transactions.
 * All data is purely synthetic — no real card data, PAN, CVV, or personal information.
 */
@Component
public class GermanyPaymentFactory {

    public enum GermanyCity {
        BERLIN("Berlin", 52.5200, 13.4050),
        HAMBURG("Hamburg", 53.5511, 9.9937),
        MUNICH("Munich", 48.1351, 11.5820),
        FRANKFURT("Frankfurt", 50.1109, 8.6821),
        COLOGNE("Cologne", 50.9333, 6.9500),
        STUTTGART("Stuttgart", 48.7758, 9.1829),
        LEIPZIG("Leipzig", 51.3397, 12.3731),
        DUSSELDORF("Düsseldorf", 51.2217, 6.7762);

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

    /** Generate a synthetic card token (safe, not a real card number). */
    public String randomCardToken() {
        return "TOK-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    /** Generate a synthetic masked PAN. */
    public String maskedPan(String last4) {
        return "**** **** **** " + last4;
    }

    /** Generate a synthetic last4. */
    public String randomLast4() {
        return String.format("%04d", RANDOM.nextInt(10000));
    }

    /** Create a normal payment transaction in a given city. */
    public PaymentTransaction normalTransaction(String cardToken, GermanyCity city) {
        String last4 = randomLast4();
        return PaymentTransaction.builder()
                .transactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .cardToken(cardToken)
                .maskedPan(maskedPan(last4))
                .last4(last4)
                .customerId("CUST-DE-" + String.format("%06d", RANDOM.nextInt(999999)))
                .merchantId("MER-" + city.name.toUpperCase().substring(0, 3) + "-001")
                .merchantName(city.name + " Retailer")
                .terminalId("TERM-" + RANDOM.nextInt(9999))
                .channel(PaymentChannel.POS)
                .amount(BigDecimal.valueOf(20 + RANDOM.nextInt(200)))
                .currency("EUR")
                .city(city.name)
                .country("DE")
                .latitude(city.lat + (RANDOM.nextDouble() - 0.5) * 0.01)
                .longitude(city.lon + (RANDOM.nextDouble() - 0.5) * 0.01)
                .eventTime(Instant.now())
                .paymentStatus("PENDING")
                .idempotencyKey(UUID.randomUUID().toString())
                .build();
    }

    /** Create impossible card travel: Berlin → Hamburg in 2 minutes. */
    public List<PaymentTransaction> impossibleTravelScenario(String cardToken) {
        List<PaymentTransaction> txns = new ArrayList<>();
        txns.add(normalTransaction(cardToken, GermanyCity.BERLIN));
        // Hamburg 2 minutes later — 288 km distance, impossible by card
        txns.add(PaymentTransaction.builder()
                .transactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .cardToken(cardToken)
                .maskedPan("**** **** **** 9999")
                .last4("9999")
                .customerId("CUST-DE-TRAVEL-001")
                .merchantId("MER-HAM-002")
                .merchantName("Hamburg Store")
                .terminalId("TERM-9001")
                .channel(PaymentChannel.POS)
                .amount(BigDecimal.valueOf(350.00))
                .currency("EUR")
                .city(GermanyCity.HAMBURG.name)
                .country("DE")
                .latitude(GermanyCity.HAMBURG.lat)
                .longitude(GermanyCity.HAMBURG.lon)
                .eventTime(Instant.now().plusSeconds(120))
                .paymentStatus("PENDING")
                .idempotencyKey(UUID.randomUUID().toString())
                .build());
        return txns;
    }

    /** Create duplicate payment scenario: same idempotency key twice. */
    public List<PaymentTransaction> duplicatePaymentScenario(String cardToken) {
        String idempotencyKey = "IDEM-" + UUID.randomUUID().toString().substring(0, 8);
        PaymentTransaction original = normalTransaction(cardToken, GermanyCity.FRANKFURT);
        original.setIdempotencyKey(idempotencyKey);

        PaymentTransaction duplicate = normalTransaction(cardToken, GermanyCity.FRANKFURT);
        duplicate.setIdempotencyKey(idempotencyKey);
        duplicate.setTransactionId("TXN-DUP-" + UUID.randomUUID().toString().substring(0, 6));
        duplicate.setEventTime(Instant.now().plusSeconds(5));

        return List.of(original, duplicate);
    }

    /** Create velocity fraud: 6 transactions in 3 minutes. */
    public List<PaymentTransaction> velocityFraudScenario(String cardToken) {
        List<PaymentTransaction> txns = new ArrayList<>();
        GermanyCity city = GermanyCity.BERLIN;
        for (int i = 0; i < 6; i++) {
            PaymentTransaction tx = normalTransaction(cardToken, city);
            tx.setEventTime(Instant.now().plusSeconds(i * 30L));
            tx.setAmount(BigDecimal.valueOf(50 + i * 10));
            txns.add(tx);
        }
        return txns;
    }

    /** Create 3DS challenge scenario: online transaction with new terminal + high amount. */
    public PaymentTransaction challengeScenario(String cardToken) {
        String last4 = randomLast4();
        return PaymentTransaction.builder()
                .transactionId("TXN-3DS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .cardToken(cardToken)
                .maskedPan(maskedPan(last4))
                .last4(last4)
                .customerId("CUST-DE-3DS-001")
                .merchantId("MER-ONLINE-003")
                .merchantName("Online Electronics Store")
                .terminalId("TERM-ONLINE-NEW-" + RANDOM.nextInt(999))
                .channel(PaymentChannel.ONLINE)
                .amount(BigDecimal.valueOf(899.99))
                .currency("EUR")
                .city(GermanyCity.MUNICH.name)
                .country("DE")
                .latitude(GermanyCity.MUNICH.lat)
                .longitude(GermanyCity.MUNICH.lon)
                .eventTime(Instant.now())
                .paymentStatus("PENDING")
                .threeDsStatus("PENDING")
                .ipCountry("RO")  // IP from Romania — geo mismatch
                .idempotencyKey(UUID.randomUUID().toString())
                .build();
    }

    public GermanyCity randomCity() {
        GermanyCity[] cities = GermanyCity.values();
        return cities[RANDOM.nextInt(cities.length)];
    }
}
