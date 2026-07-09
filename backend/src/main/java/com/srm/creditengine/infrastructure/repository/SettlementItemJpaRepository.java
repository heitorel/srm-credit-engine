package com.srm.creditengine.infrastructure.repository;

import com.srm.creditengine.infrastructure.persistence.SettlementItemEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementItemJpaRepository extends JpaRepository<SettlementItemEntity, String> {

  List<SettlementItemEntity> findBySettlement_IdOrderByCreatedAtAscIdAsc(String settlementId);
}
