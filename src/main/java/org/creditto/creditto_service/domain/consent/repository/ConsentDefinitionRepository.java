package org.creditto.creditto_service.domain.consent.repository;

import org.creditto.creditto_service.domain.consent.entity.ConsentDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConsentDefinitionRepository extends JpaRepository<ConsentDefinition, Long> {

    // Code + Version 으로 특정 버전 조회
    Optional<ConsentDefinition> findByConsentCodeAndConsentDefVer(String consentCode, Integer consentDefVer);

    // Code 기준 동의서 최신 버전 1개 조회 (JPA Derived Query)
    Optional<ConsentDefinition> findTopByConsentCodeOrderByConsentDefVerDesc(String consentCode);


    // 현재 유효한 동의서 목록 조회
    @Query("""
            SELECT d
            FROM ConsentDefinition d
            WHERE d.validFrom <= :now
                AND (d.validTo IS NULL OR d.validTo >= :now)
            """)
    List<ConsentDefinition> findActiveDefinitions(@Param("now")LocalDateTime now);

    // Code 기준 현재 유효한 최신 동의서 조회
    @Query("""
            SELECT d
            FROM ConsentDefinition d
            WHERE d.consentCode = :code
                AND d.validFrom <= :now
                AND (d.validTo IS NULL OR d.validTo >= :now)
            ORDER BY d.consentDefVer DESC
            LIMIT 1
            """)
    Optional<ConsentDefinition> findLatestActiveDefinitionByCode(@Param("code") String code, @Param("now") LocalDateTime now);

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
