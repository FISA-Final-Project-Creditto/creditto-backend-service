package org.creditto.creditto_service.domain.consent.repository;

import org.creditto.creditto_service.domain.consent.entity.ConsentDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConsentDefinitionRepository extends JpaRepository<ConsentDefinition, Long> {


    // Code 기준 동의서 최신 버전 1개 조회 (JPA Derived Query)
    Optional<ConsentDefinition> findTopByConsentCodeOrderByConsentDefVerDesc(String consentCode);

    @Query("""
            SELECT d
            FROM ConsentDefinition d
            WHERE d.consentDefVer = (
                SELECT MAX(d2.consentDefVer)
                FROM ConsentDefinition d2
                WHERE d2.consentCode = d.consentCode
            )
            """)
    List<ConsentDefinition> findLatestForAllCodes();
}
