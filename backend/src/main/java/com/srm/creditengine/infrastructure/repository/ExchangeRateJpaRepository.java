package com.srm.creditengine.infrastructure.repository;

import com.srm.creditengine.infrastructure.persistence.ExchangeRateEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExchangeRateJpaRepository extends JpaRepository<ExchangeRateEntity, String> {

    Optional<ExchangeRateEntity> findFirstBySourceCurrency_CodeAndTargetCurrency_CodeOrderByValidAtDescCreatedAtDescIdDesc(
            String sourceCurrency,
            String targetCurrency
    );
}
