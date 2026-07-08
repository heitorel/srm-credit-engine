package com.srm.creditengine.infrastructure.repository;

import com.srm.creditengine.infrastructure.persistence.SettlementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementJpaRepository extends JpaRepository<SettlementEntity, String> {
}
