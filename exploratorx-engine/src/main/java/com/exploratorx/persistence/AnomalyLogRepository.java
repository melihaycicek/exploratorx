package com.exploratorx.persistence;

import com.exploratorx.persistence.entity.AnomalyLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA repository for anomaly log entries.
 */
@Repository
public interface AnomalyLogRepository extends JpaRepository<AnomalyLogEntity, Long> {

    Page<AnomalyLogEntity> findAllByOrderByDetectedAtDesc(Pageable pageable);

    List<AnomalyLogEntity> findByModeOrderByDetectedAtDesc(String mode, Pageable pageable);

    List<AnomalyLogEntity> findByEntityIdOrderByDetectedAtDesc(String entityId);

    long countByMode(String mode);

    long countByModeAndDecision(String mode, String decision);
}
