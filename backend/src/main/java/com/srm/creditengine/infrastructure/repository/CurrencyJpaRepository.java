package com.srm.creditengine.infrastructure.repository;

import com.srm.creditengine.infrastructure.persistence.CurrencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrencyJpaRepository extends JpaRepository<CurrencyEntity, String> {
}
