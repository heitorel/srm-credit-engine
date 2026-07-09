package com.srm.creditengine.infrastructure.repository;

import com.srm.creditengine.infrastructure.persistence.ReceivableTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceivableTypeJpaRepository extends JpaRepository<ReceivableTypeEntity, String> {}
