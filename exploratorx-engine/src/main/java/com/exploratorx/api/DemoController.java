package com.exploratorx.api;

import com.exploratorx.cdr.demo.CdrScenarioService;
import com.exploratorx.pay.demo.PaymentScenarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST API for demo scenario control.
 *
 * POST /api/demo/cdr/normal        — Start CDR Normal Flow
 * POST /api/demo/cdr/suspicious    — Start CDR Suspicious Movement
 * POST /api/demo/cdr/impossible    — Start CDR Impossible Signal
 * POST /api/demo/cdr/split         — Start CDR Split Signal
 * POST /api/demo/pay/normal        — Start Payment Normal Flow
 * POST /api/demo/pay/impossible    — Start Payment Impossible Transaction
 * POST /api/demo/pay/duplicate     — Start Duplicate Payment
 * POST /api/demo/pay/velocity      — Start Velocity Fraud
 * POST /api/demo/pay/challenge     — Start 3DS Challenge Scenario
 * POST /api/demo/reset             — Reset Demo
 */
@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DemoController {

    private final CdrScenarioService cdrScenarioService;
    private final PaymentScenarioService paymentScenarioService;
    private final JdbcTemplate jdbcTemplate;

    // CDR Scenarios
    @PostMapping("/cdr/normal")
    public ResponseEntity<Map<String, String>> startCdrNormal() {
        log.info("API: Starting CDR Normal Flow");
        cdrScenarioService.runNormalFlow();
        return ok("CDR Normal Flow started");
    }

    @PostMapping("/cdr/suspicious")
    public ResponseEntity<Map<String, String>> startCdrSuspicious() {
        log.info("API: Starting CDR Suspicious Movement");
        cdrScenarioService.runSuspiciousMovement();
        return ok("CDR Suspicious Movement started");
    }

    @PostMapping("/cdr/impossible")
    public ResponseEntity<Map<String, String>> startCdrImpossible() {
        log.info("API: Starting CDR Impossible Signal");
        cdrScenarioService.runImpossibleSignal();
        return ok("CDR Impossible Signal started");
    }

    @PostMapping("/cdr/split")
    public ResponseEntity<Map<String, String>> startCdrSplit() {
        log.info("API: Starting CDR Split Signal");
        cdrScenarioService.runSplitSignal();
        return ok("CDR Split Signal started");
    }

    // Payment Scenarios
    @PostMapping("/pay/normal")
    public ResponseEntity<Map<String, String>> startPayNormal() {
        log.info("API: Starting Payment Normal Flow");
        paymentScenarioService.runNormalFlow();
        return ok("Payment Normal Flow started");
    }

    @PostMapping("/pay/impossible")
    public ResponseEntity<Map<String, String>> startPayImpossible() {
        log.info("API: Starting Payment Impossible Transaction");
        paymentScenarioService.runImpossibleTransaction();
        return ok("Payment Impossible Transaction started");
    }

    @PostMapping("/pay/duplicate")
    public ResponseEntity<Map<String, String>> startPayDuplicate() {
        log.info("API: Starting Duplicate Payment");
        paymentScenarioService.runDuplicatePayment();
        return ok("Duplicate Payment started");
    }

    @PostMapping("/pay/velocity")
    public ResponseEntity<Map<String, String>> startPayVelocity() {
        log.info("API: Starting Velocity Fraud");
        paymentScenarioService.runVelocityFraud();
        return ok("Velocity Fraud started");
    }

    @PostMapping("/pay/challenge")
    public ResponseEntity<Map<String, String>> startPayChallenge() {
        log.info("API: Starting 3DS Challenge Scenario");
        paymentScenarioService.runChallengeScenario();
        return ok("3DS Challenge Scenario started");
    }

    // Backfill (Debezium incremental snapshot signal)
    @PostMapping("/backfill/cdr")
    public ResponseEntity<Map<String, String>> backfillCdr() {
        log.info("API: Triggering CDR historical backfill via Debezium incremental snapshot");
        jdbcTemplate.update(
                "INSERT INTO debezium_signal_cdr(id, type, data) VALUES (?, ?, ?) ON CONFLICT DO NOTHING",
                "ad-hoc-cdr-backfill-" + System.currentTimeMillis(),
                "execute-snapshot",
                "{\"data-collections\": [\"public.cdr_signal\"]}"
        );
        return ok("CDR Historical Backfill signal sent");
    }

    @PostMapping("/backfill/pay")
    public ResponseEntity<Map<String, String>> backfillPay() {
        log.info("API: Triggering Payment historical backfill via Debezium incremental snapshot");
        jdbcTemplate.update(
                "INSERT INTO debezium_signal_pay(id, type, data) VALUES (?, ?, ?) ON CONFLICT DO NOTHING",
                "ad-hoc-pay-backfill-" + System.currentTimeMillis(),
                "execute-snapshot",
                "{\"data-collections\": [\"public.payment_transaction\"]}"
        );
        return ok("Payment Historical Backfill signal sent");
    }

    // Reset
    @PostMapping("/reset")
    public ResponseEntity<Map<String, String>> resetDemo() {
        log.warn("API: Resetting demo — truncating cdr_signal and payment_transaction tables");
        jdbcTemplate.execute("TRUNCATE TABLE cdr_signal RESTART IDENTITY CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE payment_transaction RESTART IDENTITY CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE anomaly_log RESTART IDENTITY CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE demo_run RESTART IDENTITY CASCADE");
        return ok("Demo reset complete");
    }

    private ResponseEntity<Map<String, String>> ok(String message) {
        return ResponseEntity.ok(Map.of("status", "ok", "message", message));
    }
}
