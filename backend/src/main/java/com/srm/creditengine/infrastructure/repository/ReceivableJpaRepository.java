package com.srm.creditengine.infrastructure.repository;

import com.srm.creditengine.infrastructure.persistence.ReceivableEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceivableJpaRepository extends JpaRepository<ReceivableEntity, String> {

  Optional<ReceivableEntity> findByAssignor_IdAndExternalReference(
      String assignorId, String externalReference);
}
