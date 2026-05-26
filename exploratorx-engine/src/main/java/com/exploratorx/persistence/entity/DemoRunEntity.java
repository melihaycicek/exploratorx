package com.exploratorx.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * JPA entity for demo run tracking.
 * Maps to the demo_run table in PostgreSQL.
 */
@Entity
@Table(name = "demo_run")
@Data
@NoArgsConstructor
public class DemoRunEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "run_id", nullable = false, unique = true, length = 64)
    private String runId;

    @Column(name = "mode", length = 16)
    private String mode;

    @Column(name = "scenario", length = 64)
    private String scenario;

    @Column(name = "started_at")
    private Instant startedAt = Instant.now();

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "status", length = 16)
    private String status = "RUNNING";
}
