package com.exploratorx.persistence;

import com.exploratorx.persistence.entity.DemoRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA repository for demo run tracking.
 */
@Repository
public interface DemoRunRepository extends JpaRepository<DemoRunEntity, Long> {

    Optional<DemoRunEntity> findByRunId(String runId);

    Optional<DemoRunEntity> findFirstByStatusOrderByStartedAtDesc(String status);
}
