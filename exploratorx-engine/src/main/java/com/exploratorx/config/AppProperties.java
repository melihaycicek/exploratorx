package com.exploratorx.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Typed configuration properties for ExploratorX engine.
 * Bound from application.yml under the 'exploratorx' prefix.
 */
@Component
@ConfigurationProperties(prefix = "exploratorx")
@Data
public class AppProperties {

    private Thresholds thresholds = new Thresholds();
    private Demo demo = new Demo();
    private Topics topics = new Topics();

    @Data
    public static class Thresholds {
        private Cdr cdr = new Cdr();
        private Pay pay = new Pay();

        @Data
        public static class Cdr {
            private double impossibleSpeedKmh = 900;
            private double suspiciousSpeedKmh = 300;
            private int splitSignalWindowSeconds = 60;
        }

        @Data
        public static class Pay {
            private double impossibleSpeedKmh = 500;
            private int velocityWindowMinutes = 5;
            private int velocityMaxTransactions = 5;
        }
    }

    @Data
    public static class Demo {
        private long signalIntervalMs = 1500;
    }

    @Data
    public static class Topics {
        private CdrTopics cdr = new CdrTopics();
        private PayTopics pay = new PayTopics();
        private String audit = "exploratorx.audit";
        private String dlq = "exploratorx.dlq";

        @Data
        public static class CdrTopics {
            private String raw = "exploratorx.cdr.public.cdr_signal";
            private String clean = "exploratorx.cdr.signals.clean";
            private String anomalies = "exploratorx.cdr.anomalies";
        }

        @Data
        public static class PayTopics {
            private String raw = "exploratorx.pay.public.payment_transaction";
            private String clean = "exploratorx.pay.transactions.clean";
            private String fraudAlerts = "exploratorx.pay.fraud.alerts";
        }
    }
}
