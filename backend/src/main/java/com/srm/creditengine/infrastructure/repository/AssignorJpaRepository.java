package com.srm.creditengine.infrastructure.repository;

import com.srm.creditengine.infrastructure.persistence.AssignorEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignorJpaRepository extends JpaRepository<AssignorEntity, String> {

    Optional<AssignorEntity> findFirstByDocument(String document);
}
